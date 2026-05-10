package com.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.entity.ActivitySchedule;
import com.service.ActivityScheduleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/activitySchedule")
public class ActivityScheduleController {

    @Resource
    private ActivityScheduleService activityScheduleService;

    /**
     * 根据活动ID查询日程列表
     */
    @GetMapping("/selectByActivityId")
    public List<ActivitySchedule> selectByActivityId(@RequestParam Integer activityId) {
        LambdaQueryWrapper<ActivitySchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySchedule::getActivityId, activityId)
                .orderByAsc(ActivitySchedule::getScheduleDate);
        return activityScheduleService.list(wrapper);
    }
}
