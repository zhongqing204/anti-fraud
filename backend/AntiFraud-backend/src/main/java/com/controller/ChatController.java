package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.ChatMessage;
import com.service.ChatMessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    
    @Resource
    private ChatMessageService chatMessageService;
    
    /**
     * 获取聊天记录
     */
    @GetMapping("/history")
    public Result getChatHistory(@RequestParam Integer userId, @RequestParam Integer otherUserId) {
        List<ChatMessage> history = chatMessageService.getChatHistory(userId, otherUserId);
        return Result.success(history);
    }
    
    /**
     * 分页获取聊天记录
     */
    @GetMapping("/history/page")
    public Result getChatHistoryPage(@RequestParam Integer userId, 
                                     @RequestParam Integer otherUserId,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<ChatMessage> page = chatMessageService.getChatHistoryPage(userId, otherUserId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/markAsRead")
    public Result markAsRead(@RequestParam Integer userId, @RequestParam Integer fromUserId) {
        chatMessageService.markAsRead(userId, fromUserId);
        return Result.success();
    }
    
    /**
     * 获取未读消息数
     */
    @GetMapping("/unreadCount")
    public Result getUnreadCount(@RequestParam Integer userId) {
        Integer count = chatMessageService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    /**
     * 获取有聊天记录的用户列表（管理员使用）
     */
    @GetMapping("/chatUsers")
    public Result getChatUsers(@RequestParam Integer adminId) {
        List<Map<String, Object>> users = chatMessageService.getChatUsers(adminId);
        return Result.success(users);
    }
    
    /**
     * 删除聊天记录（用户删除与某人的所有聊天）
     */
    @DeleteMapping("/clearHistory")
    public Result clearChatHistory(@RequestParam Integer userId, @RequestParam Integer otherUserId) {
        chatMessageService.clearChatHistory(userId, otherUserId);
        return Result.success();
    }
}
