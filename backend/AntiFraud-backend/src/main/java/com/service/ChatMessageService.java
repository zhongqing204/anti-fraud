package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatMessageService extends IService<ChatMessage> {
    
    /**
     * 获取用户聊天记录
     */
    List<ChatMessage> getChatHistory(Integer userId, Integer otherUserId);
    
    /**
     * 分页获取聊天记录
     */
    Page<ChatMessage> getChatHistoryPage(Integer userId, Integer otherUserId, Integer pageNum, Integer pageSize);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Integer userId, Integer fromUserId);
    
    /**
     * 获取未读消息数
     */
    Integer getUnreadCount(Integer userId);
    
    /**
     * 获取有聊天记录的用户列表（管理员使用）
     */
    List<Map<String, Object>> getChatUsers(Integer adminId);
    
    /**
     * 删除聊天记录
     */
    void clearChatHistory(Integer userId, Integer otherUserId);
}
