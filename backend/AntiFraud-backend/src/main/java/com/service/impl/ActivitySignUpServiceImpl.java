package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.CustomException;
import com.entity.Activity;
import com.entity.ActivitySignup;
import com.entity.User;
import com.mapper.ActivityMapper;
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

    @Override
    public void add(ActivitySignup activitySignup) {
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySignup::getActivityId, activitySignup.getActivityId())
                .eq(ActivitySignup::getUserId, activitySignup.getUserId());

        List<ActivitySignup> list = this.list(queryWrapper);
        if (!CollectionUtils.isEmpty(list)) {
            throw new CustomException("500", "您已经报名过该活动，请勿重复提交");
        } else {
            activitySignup.setSignupTime(LocalDateTime.now());
            User user = userMapper.selectById(activitySignup.getUserId());
            if (user != null) {
                activitySignup.setUserName(user.getName());
            }

            Activity activity = activityMapper.selectById(activitySignup.getActivityId());
            if (activity != null) {
                activitySignup.setActivityName(activity.getTitle());
            }
            this.save(activitySignup);
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeBatchByIds(ids);
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
    public Page<ActivitySignup> selectPage(String userName, String activityName,Integer userId, Integer pageNum, Integer pageSize) {
        Page<ActivitySignup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq(ActivitySignup::getUserId, userId);
        }

        // 根据用户名模糊查询
        if (StringUtils.hasText(userName)) {
            queryWrapper.like(ActivitySignup::getUserName, userName);
        }
        // 根据活动名称模糊查询
        if (StringUtils.hasText(activityName)) {
            queryWrapper.like(ActivitySignup::getActivityName, activityName);
        }

        queryWrapper.orderByDesc(ActivitySignup::getSignupTime);
        return this.page(page, queryWrapper);
    }
}
