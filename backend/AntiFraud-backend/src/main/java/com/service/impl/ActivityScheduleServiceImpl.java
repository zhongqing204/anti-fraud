package com.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.ActivitySchedule;
import com.mapper.ActivityScheduleMapper;
import com.service.ActivityScheduleService;
import org.springframework.stereotype.Service;

@Service
public class ActivityScheduleServiceImpl extends ServiceImpl<ActivityScheduleMapper, ActivitySchedule> implements ActivityScheduleService {
}
