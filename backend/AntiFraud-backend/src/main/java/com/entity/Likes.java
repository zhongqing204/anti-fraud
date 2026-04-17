package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("likes")
public class Likes {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer articleId;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String articleTitle;
}
