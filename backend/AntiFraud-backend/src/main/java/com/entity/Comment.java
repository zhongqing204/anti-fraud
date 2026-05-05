package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer articleId;
    private Integer videoId;
    private String content;
    private String userName;
    private String userAvatar;
    private String articleTitle;
    private String videoTitle;
    private Integer publicityId;
    private String publicityTitle;
    private Integer activityId;
    private String activityTitle;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String time;
}
