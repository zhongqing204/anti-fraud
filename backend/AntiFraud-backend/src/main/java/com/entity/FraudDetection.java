package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 诈骗识别记录实体类
 */
@Data
@TableName("fraud_detection")
public class FraudDetection {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 待检测文本内容
     */
    private String content;
    
    /**
     * 诈骗类型：network-网络诈骗, phone-电话诈骗, sms-短信诈骗, email-邮件诈骗
     */
    private String fraudType;
    
    /**
     * 风险等级：low-低风险, medium-中风险, high-高风险
     */
    private String riskLevel;
    
    /**
     * 风险分数（0-100）
     */
    private Double riskScore;
    
    /**
     * 检测到的关键词（JSON格式）
     */
    private String keywords;
    
    /**
     * 检测结果描述
     */
    private String resultDescription;
    
    /**
     * 建议措施
     */
    private String suggestion;
    
    /**
     * 检测时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime detectTime;
}
