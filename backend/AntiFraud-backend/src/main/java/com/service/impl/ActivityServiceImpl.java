package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.*;
import com.mapper.ActivityMapper;
import com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
            // ===== 新增逻辑：判断结束时间，自动更新状态为"已结束" =====
            if (item.getEndTime() != null && now.isAfter(item.getEndTime())){
                item.setStatus("已结束");
                this.updateById(item);
            }
        }
        return resultPage;
    }

    @Override
    public List<Activity> selectTop4() {
        // 查询所有活动
        List<Activity> allActivities = this.list();
        // ===== 新增逻辑：随机打乱前先更新已结束活动的状态 =====
        LocalDateTime now = LocalDateTime.now();
        for (Activity item : allActivities){
            // ===== 新增逻辑：判断结束时间，自动更新状态为"已结束" =====
            if (item.getEndTime() != null && now.isAfter(item.getEndTime())){
                item.setStatus("已结束");
                this.updateById(item);
            }
        }
        // 随机打乱列表
        Collections.shuffle(allActivities);
        // 返回前4条，如果不足4条则返回全部
        return allActivities.subList(0, Math.min(4, allActivities.size()));
    }


}
