package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_report")
public class ArticleReport {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer articleId;
    private String articleTitle;
    private String reportType;
    private String detailReason;
    private String files;
    private String status;
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
}
