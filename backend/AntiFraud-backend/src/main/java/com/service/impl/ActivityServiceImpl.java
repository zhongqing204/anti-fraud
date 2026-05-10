package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.*;
import com.mapper.ActivityMapper;
import com.mapper.ActivityScheduleMapper;
import com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LikesService likesService;

    @Autowired
    private CollectService collectService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ActivityScheduleMapper activityScheduleMapper;

    @Override
    public void add(Activity activity) {
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        if (activity.getCategoryId() != null){
            Category category = categoryService.getById(activity.getCategoryId());
            if (category != null){
                activity.setCategoryName(category.getName());
            }
        }
        
        // 设置默认值
        if (activity.getActivityDurationType() == null) {
            activity.setActivityDurationType("short"); // 默认短期活动
        }
        if (activity.getDurationDays() == null) {
            activity.setDurationDays(1);
        }
        
        // 初始化当前报名人数为0
        if (activity.getCurrentParticipants() == null) {
            activity.setCurrentParticipants(0);
        }
        // 如果没有限制人数，默认为0（不限制）
        if (activity.getMaxParticipants() == null) {
            activity.setMaxParticipants(0);
        }
        
        save(activity);
        
        // 如果是长期活动，自动生成日程
        if ("long".equals(activity.getActivityDurationType()) && activity.getStartTime() != null) {
            generateSchedules(activity);
        }
    }
    
    /**
     * 为长期活动生成日程
     */
    private void generateSchedules(Activity activity) {
        LocalDate startDate = activity.getStartTime().toLocalDate();
        LocalTime startTime = activity.getStartTime().toLocalTime();
        LocalTime endTime = activity.getEndTime().toLocalTime();
        Integer durationDays = activity.getDurationDays() != null ? activity.getDurationDays() : 1;
        
        for (int i = 0; i < durationDays; i++) {
            ActivitySchedule schedule = new ActivitySchedule();
            schedule.setActivityId(activity.getId());
            schedule.setScheduleDate(startDate.plusDays(i));
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setMaxParticipants(activity.getMaxParticipants()); // 每天的限制人数相同
            schedule.setCurrentParticipants(0);
            schedule.setCreateTime(LocalDateTime.now());
            activityScheduleMapper.insert(schedule);
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        for (Integer activityId : ids) {
            likesService.remove(new LambdaQueryWrapper<Likes>().eq(Likes::getActivityId, activityId));
            collectService.remove(new LambdaQueryWrapper<Collect>().eq(Collect::getActivityId, activityId));
            commentService.remove(new LambdaQueryWrapper<Comment>().eq(Comment::getActivityId, activityId));
        }
        this.removeByIds(ids);
    }

    @Override
    public List<Activity> selectAll(Activity activity) {
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        if (activity != null){
            if (StringUtils.hasText(activity.getTitle())){
                queryWrapper.like(Activity::getTitle,activity.getTitle());
            }
            if (activity.getCategoryId() != null){
                queryWrapper.eq(Activity::getCategoryId,activity.getCategoryId());
            }
            if (StringUtils.hasText(activity.getStatus())){
                queryWrapper.eq(Activity::getStatus,activity.getStatus());
            }
        }
        queryWrapper.orderByDesc(Activity::getCreateTime);
        List<Activity> list = this.list(queryWrapper);

        for (Activity item : list){
            if (item.getCategoryId() != null){
                Category category = categoryService.getById(item.getCategoryId());
                if (category != null){
                    item.setCategoryName(category.getName());
                }
            }
        }
        return list;
    }

    @Override
    public Page<Activity> selectPage(Activity activity, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        if (activity != null){
            if (StringUtils.hasText(activity.getTitle())){
                queryWrapper.like(Activity::getTitle,activity.getTitle());
            }
            if (activity.getCategoryId() != null){
                queryWrapper.eq(Activity::getCategoryId,activity.getCategoryId());
            }
            if (StringUtils.hasText(activity.getStatus())){
                queryWrapper.eq(Activity::getStatus,activity.getStatus());
            }
        }
        queryWrapper.orderByDesc(Activity::getCreateTime);
        Page<Activity> resultPage = this.baseMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        LocalDateTime now = LocalDateTime.now();
        for (Activity item : resultPage.getRecords()){
            if (item.getCategoryId() != null){
                Category category = categoryService.getById(item.getCategoryId());
                if (category != null){
                    item.setCategoryName(category.getName());
                }
            }
            // ===== 新增逻辑：根据开始和结束时间自动更新状态 =====
            String newStatus = calculateStatus(item.getStartTime(), item.getEndTime(), now);
            if (!newStatus.equals(item.getStatus())) {
                item.setStatus(newStatus);
                this.updateById(item);
            }
        }
        return resultPage;
    }

    @Override
    public List<Activity> selectTop4() {
        // 查询所有活动
        List<Activity> allActivities = this.list();
        // ===== 新增逻辑：随机打乱前先更新活动状态 =====
        LocalDateTime now = LocalDateTime.now();
        for (Activity item : allActivities){
            // ===== 新增逻辑：根据开始和结束时间自动更新状态 =====
            String newStatus = calculateStatus(item.getStartTime(), item.getEndTime(), now);
            if (!newStatus.equals(item.getStatus())) {
                item.setStatus(newStatus);
                this.updateById(item);
            }
        }
        // 随机打乱列表
        Collections.shuffle(allActivities);
        // 返回前4条，如果不足4条则返回全部
        return allActivities.subList(0, Math.min(4, allActivities.size()));
    }

    /**
     * 根据开始时间和结束时间计算活动状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param now 当前时间
     * @return 状态：未开始、进行中、已结束
     */
    private String calculateStatus(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        if (startTime != null && now.isBefore(startTime)) {
            return "未开始";
        } else if (endTime != null && now.isAfter(endTime)) {
            return "已结束";
        } else {
            return "进行中";
        }
    }


}
