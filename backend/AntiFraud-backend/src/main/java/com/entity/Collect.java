package com.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("collect")
public class Collect {
    private Integer id;
    private Integer userId;
    private Integer articleId;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String articleTitle;

    @TableField(exist = false)
    private Article article;
}
