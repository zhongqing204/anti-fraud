package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.CustomException;
import com.entity.Activity;
import com.entity.ActivitySchedule;
import com.entity.ActivitySignup;
import com.entity.User;
import com.mapper.ActivityMapper;
import com.mapper.ActivityScheduleMapper;
import com.mapper.ActivitySignUpMapper;
import com.mapper.UserMapper;
import com.service.ActivitySignUpService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivitySignUpServiceImpl extends ServiceImpl<ActivitySignUpMapper, ActivitySignup> implements ActivitySignUpService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ActivityScheduleMapper activityScheduleMapper;

    @Override
    public void add(ActivitySignup activitySignup) {
        // 检查是否已报名（长期活动需要检查同一天）
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySignup::getActivityId, activitySignup.getActivityId())
                .eq(ActivitySignup::getUserId, activitySignup.getUserId());
        
        // 如果是长期活动，还需要检查日期
        if (activitySignup.getScheduleDate() != null) {
            queryWrapper.eq(ActivitySignup::getScheduleDate, activitySignup.getScheduleDate());
        }

        List<ActivitySignup> list = this.list(queryWrapper);
        if (!CollectionUtils.isEmpty(list)) {
            throw new CustomException("400", "您已经报名过该活动" + 
                (activitySignup.getScheduleDate() != null ? "的" + activitySignup.getScheduleDate() + "这一天" : "") + 
                "，请勿重复提交");
        }
        
        // 检查活动信息
        Activity activity = activityMapper.selectById(activitySignup.getActivityId());
        if (activity == null) {
            throw new CustomException("400", "活动不存在");
        }
        
        // 根据活动类型检查人数限制
        if ("long".equals(activity.getActivityDurationType())) {
            // 长期活动：检查指定日期的名额
            checkLongActivitySchedule(activitySignup);
        } else {
            // 短期活动：检查总名额
            checkShortActivityLimit(activity);
        }
        
        // 设置报名信息
        activitySignup.setSignupTime(LocalDateTime.now());
        User user = userMapper.selectById(activitySignup.getUserId());
        if (user != null) {
            activitySignup.setUserName(user.getName());
        }
        activitySignup.setActivityName(activity.getTitle());
        this.save(activitySignup);
        
        // 更新报名人数
        updateParticipantsCount(activitySignup, activity, true);
    }
    
    /**
     * 检查长期活动的日程名额
     */
    private void checkLongActivitySchedule(ActivitySignup activitySignup) {
        if (activitySignup.getScheduleDate() == null) {
            throw new CustomException("400", "长期活动必须选择报名日期");
        }
        
        LambdaQueryWrapper<ActivitySchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(ActivitySchedule::getActivityId, activitySignup.getActivityId())
                .eq(ActivitySchedule::getScheduleDate, activitySignup.getScheduleDate());
        ActivitySchedule schedule = activityScheduleMapper.selectOne(scheduleWrapper);
        
        if (schedule == null) {
            throw new CustomException("400", "选择的日期没有活动安排");
        }
        
        // 检查该天的人数限制
        if (schedule.getMaxParticipants() != null && schedule.getMaxParticipants() > 0) {
            if (schedule.getCurrentParticipants() != null && 
                schedule.getCurrentParticipants() >= schedule.getMaxParticipants()) {
                throw new CustomException("400", 
                    schedule.getScheduleDate() + " 这一天的报名人数已满，无法继续报名");
            }
        }
    }
    
    /**
     * 检查短期活动的人数限制
     */
    private void checkShortActivityLimit(Activity activity) {
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
            if (activity.getCurrentParticipants() != null && 
                activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                throw new CustomException("400", "报名人数已满，无法继续报名");
            }
        }
    }
    
    /**
     * 更新报名人数统计
     */
    private void updateParticipantsCount(ActivitySignup signup, Activity activity, boolean isAdd) {
        if ("long".equals(activity.getActivityDurationType())) {
            // 长期活动：更新对应日期的报名人数
            if (signup.getScheduleDate() != null) {
                LambdaQueryWrapper<ActivitySchedule> scheduleWrapper = new LambdaQueryWrapper<>();
                scheduleWrapper.eq(ActivitySchedule::getActivityId, signup.getActivityId())
                        .eq(ActivitySchedule::getScheduleDate, signup.getScheduleDate());
                ActivitySchedule schedule = activityScheduleMapper.selectOne(scheduleWrapper);
                
                if (schedule != null) {
                    if (schedule.getCurrentParticipants() == null) {
                        schedule.setCurrentParticipants(0);
                    }
                    schedule.setCurrentParticipants(schedule.getCurrentParticipants() + (isAdd ? 1 : -1));
                    activityScheduleMapper.updateById(schedule);
                }
            }
        } else {
            // 短期活动：更新总报名人数
            if (activity.getCurrentParticipants() == null) {
                activity.setCurrentParticipants(0);
            }
            activity.setCurrentParticipants(activity.getCurrentParticipants() + (isAdd ? 1 : -1));
            activityMapper.updateById(activity);
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        // 删除报名记录前，先减少活动的当前报名人数
        for (Integer id : ids) {
            ActivitySignup signup = this.getById(id);
            if (signup != null) {
                Activity activity = activityMapper.selectById(signup.getActivityId());
                if (activity != null) {
                    updateParticipantsCount(signup, activity, false);
                }
            }
        }
        this.removeBatchByIds(ids);
    }

    /**
     * 申请取消报名
     */
    public void applyCancel(Integer signupId, String cancelReason) {
        ActivitySignup signup = this.getById(signupId);
        if (signup == null) {
            throw new CustomException("400", "报名记录不存在");
        }
        
        if (!"审核通过".equals(signup.getStatus())) {
            throw new CustomException("400", "只有审核通过的报名才能申请取消");
        }
        
        if ("待审批".equals(signup.getCancelStatus())) {
            throw new CustomException("400", "已有待审批的取消申请");
        }
        
        signup.setCancelReason(cancelReason);
        signup.setCancelStatus("待审批");
        signup.setCancelTime(LocalDateTime.now());
        this.updateById(signup);
    }

    /**
     * 审批取消报名申请
     */
    public void approveCancel(Integer signupId, Boolean approved, String reason) {
        ActivitySignup signup = this.getById(signupId);
        if (signup == null) {
            throw new CustomException("400", "报名记录不存在");
        }
        
        if (!"待审批".equals(signup.getCancelStatus())) {
            throw new CustomException("400", "没有待审批的取消申请");
        }
        
        if (approved) {
            // 同意取消，删除报名记录并减少人数
            Activity activity = activityMapper.selectById(signup.getActivityId());
            if (activity != null) {
                updateParticipantsCount(signup, activity, false);
            }
            this.removeById(signupId);
        } else {
            // 拒绝取消，清空取消状态
            signup.setCancelStatus(null);
            signup.setCancelReason(null);
            signup.setCancelTime(null);
            this.updateById(signup);
        }
    }

    @Override
    public List<ActivitySignup> selectAll(ActivitySignup activitySignup) {
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();

        if (activitySignup != null) {
            if (activitySignup.getUserId() != null) {
                queryWrapper.eq(ActivitySignup::getUserId, activitySignup.getUserId());
            }
            if (activitySignup.getActivityId() != null) {
                queryWrapper.eq(ActivitySignup::getActivityId, activitySignup.getActivityId());
            }
            if (StringUtils.hasText(activitySignup.getStatus())) {
                queryWrapper.eq(ActivitySignup::getStatus, activitySignup.getStatus());
            }
        }

        queryWrapper.orderByDesc(ActivitySignup::getSignupTime);
        return this.list(queryWrapper);
    }

    @Override
    public Page<ActivitySignup> selectPage(String userName, String realName, Integer activityId, String status, Integer pageNum, Integer pageSize) {
        Page<ActivitySignup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();

        // 根据用户名模糊查询
        if (StringUtils.hasText(userName)) {
            queryWrapper.like(ActivitySignup::getUserName, userName);
        }
        
        // 根据真实姓名模糊查询
        if (StringUtils.hasText(realName)) {
            queryWrapper.like(ActivitySignup::getRealName, realName);
        }
        
        // 根据活动ID精确查询
        if (activityId != null) {
            queryWrapper.eq(ActivitySignup::getActivityId, activityId);
        }
        
        // 根据审核状态查询
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(ActivitySignup::getStatus, status);
        }

        queryWrapper.orderByDesc(ActivitySignup::getSignupTime);
        return this.page(page, queryWrapper);
    }
}
