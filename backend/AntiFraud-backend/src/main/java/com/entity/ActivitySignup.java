package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("activity_signup")
public class ActivitySignup {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer activityId;
    private Integer userId;
    private String realName;
    private String phone;
    private String email;
    private String gender;
    private Integer age;
    private String organization;
    private String remark;
    private String status;
    private String reason;
    private String cancelReason; // 取消原因
    private String cancelStatus; // 取消状态：待审批、已同意、已拒绝
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime; // 申请取消时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signupTime;
    private String userName;
    private String activityName;
    private LocalDate scheduleDate; // 报名的日期（仅长期活动使用）
}
