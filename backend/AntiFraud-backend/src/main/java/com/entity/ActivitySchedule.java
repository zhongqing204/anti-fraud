package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("activity_schedule")
public class ActivitySchedule {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer activityId;
    private LocalDate scheduleDate; // 日期
    private LocalTime startTime; // 开始时间
    private LocalTime endTime; // 结束时间
    private Integer maxParticipants; // 该天限制人数
    private Integer currentParticipants; // 该天当前报名人数
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
