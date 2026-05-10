package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天记录实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 发送者ID
     */
    private Integer fromUserId;
    
    /**
     * 发送者姓名
     */
    private String fromUserName;
    
    /**
     * 发送者头像
     */
    private String fromUserAvatar;
    
    /**
     * 接收者ID（0表示群聊/管理员）
     */
    private Integer toUserId;
    
    /**
     * 接收者姓名
     */
    private String toUserName;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：text-文本, image-图片, system-系统消息
     */
    private String messageType;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
}
