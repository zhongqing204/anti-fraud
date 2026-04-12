package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.ActivitySignup;

import java.util.List;

public interface ActivitySignUpService extends IService<ActivitySignup> {
    void add(ActivitySignup activitySignup);

    void deleteBatch(List<Integer> ids);

    List<ActivitySignup> selectAll(ActivitySignup activitySignup);

    Page<ActivitySignup> selectPage(String userName, String activityName,Integer userId, Integer pageNum, Integer pageSize);
}
