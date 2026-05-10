package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.ActivitySignup;

import java.util.List;

public interface ActivitySignUpService extends IService<ActivitySignup> {
    void add(ActivitySignup activitySignup);

    void deleteBatch(List<Integer> ids);

    List<ActivitySignup> selectAll(ActivitySignup activitySignup);

    Page<ActivitySignup> selectPage(String userName, String realName, Integer activityId, String status, Integer pageNum, Integer pageSize);

    /**
     * 申请取消报名
     */
    void applyCancel(Integer signupId, String cancelReason);

    /**
     * 审批取消报名申请
     */
    void approveCancel(Integer signupId, Boolean approved, String reason);
}
