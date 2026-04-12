package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_signup")
public class ActivitySignup {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer activityId;
    private Integer userId;
    private String status;
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signupTime;
    private String userName;
    private String activityName;
}
