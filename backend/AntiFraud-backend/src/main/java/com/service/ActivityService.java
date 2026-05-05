package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Activity;

import java.util.List;

public interface ActivityService extends IService<Activity> {
    void add(Activity activity);

    void deleteBatch(List<Integer> ids);

    List<Activity> selectAll(Activity activity);

    Page<Activity> selectPage(Activity activity, Integer pageNum, Integer pageSize);

    List<Activity> selectTop4();

}
