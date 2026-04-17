package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
    private Integer userId;
    private String status;
    private String userName;
    private String userAvatar;

    @TableField(exist = false)
    private Integer likeCount;
    @TableField(exist = false)
    private Integer commentCount;
    @TableField(exist = false)
    private Integer collectCount;
    @TableField(exist = false)
    private Boolean liked;
    @TableField(exist = false)
    private Boolean collected;

}
