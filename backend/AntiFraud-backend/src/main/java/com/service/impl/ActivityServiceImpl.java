package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Activity;
import com.entity.Category;
import com.mapper.ActivityMapper;
import com.service.ActivityService;
import com.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private CategoryService categoryService;

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
        save(activity);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
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
        Page<Activity> page = new Page<>(pageNum, pageSize);
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
        Page<Activity> resultPage = this.page(page, queryWrapper);
        for (Activity item : resultPage.getRecords()){
            if (item.getCategoryId() != null){
                Category category = categoryService.getById(item.getCategoryId());
                if (category != null){
                    item.setCategoryName(category.getName());
                }
            }
        }
        return resultPage;
    }
}
