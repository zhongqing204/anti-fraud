package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.config.ChatWebSocket;
import com.entity.ChatMessage;
import com.entity.User;
import com.mapper.ChatMessageMapper;
import com.service.ChatMessageService;
import com.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    
    @Resource
    private UserService userService;
    
    @Override
    public List<ChatMessage> getChatHistory(Integer userId, Integer otherUserId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果 otherUserId 为 0，表示查询该用户的所有聊天（包括广播和与所有管理员的私聊）
        if (otherUserId == 0) {
            queryWrapper.and(wrapper -> 
                wrapper.or(w -> w.eq(ChatMessage::getFromUserId, userId))  // 用户发送的所有消息（广播或私聊）
                       .or(w -> w.eq(ChatMessage::getToUserId, userId))     // 发给用户的所有消息（管理员回复）
            );
        } else {
            // 私聊模式：查询两个特定用户之间的聊天
            queryWrapper.and(wrapper -> 
                wrapper.or(w -> w.eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getToUserId, otherUserId))
                       .or(w -> w.eq(ChatMessage::getFromUserId, otherUserId).eq(ChatMessage::getToUserId, userId))
                       .or(w -> w.eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getToUserId, 0))
                       .or(w -> w.eq(ChatMessage::getFromUserId, otherUserId).eq(ChatMessage::getToUserId, 0))
            );
        }
        
        queryWrapper.orderByAsc(ChatMessage::getSendTime);
        List<ChatMessage> messages = this.list(queryWrapper);
        
        // 填充用户头像
        fillUserAvatars(messages);
        
        return messages;
    }
    
    @Override
    public Page<ChatMessage> getChatHistoryPage(Integer userId, Integer otherUserId, Integer pageNum, Integer pageSize) {
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果 otherUserId 为 0，表示查询该用户的所有聊天
        if (otherUserId == 0) {
            queryWrapper.and(wrapper -> 
                wrapper.or(w -> w.eq(ChatMessage::getFromUserId, userId))
                       .or(w -> w.eq(ChatMessage::getToUserId, userId))
            );
        } else {
            // 私聊模式：查询两个特定用户之间的聊天
            queryWrapper.and(wrapper -> 
                wrapper.or(w -> w.eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getToUserId, otherUserId))
                       .or(w -> w.eq(ChatMessage::getFromUserId, otherUserId).eq(ChatMessage::getToUserId, userId))
                       .or(w -> w.eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getToUserId, 0))
                       .or(w -> w.eq(ChatMessage::getFromUserId, otherUserId).eq(ChatMessage::getToUserId, 0))
            );
        }
        
        queryWrapper.orderByDesc(ChatMessage::getSendTime);
        return this.page(page, queryWrapper);
    }
    
    @Override
    public void markAsRead(Integer userId, Integer fromUserId) {
        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatMessage::getToUserId, userId)
                    .eq(ChatMessage::getFromUserId, fromUserId)
                    .eq(ChatMessage::getIsRead, 0)
                    .set(ChatMessage::getIsRead, 1);
        this.update(updateWrapper);
    }
    
    @Override
    public Integer getUnreadCount(Integer userId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        // 查询发给该用户的未读消息（包括私聊和广播）
        queryWrapper.and(wrapper -> 
            wrapper.or(w -> w.eq(ChatMessage::getToUserId, userId))  // 私聊发给userId
                   .or(w -> w.eq(ChatMessage::getToUserId, 0))       // 广播消息
        );
        queryWrapper.eq(ChatMessage::getIsRead, 0);
        return (int) this.count(queryWrapper);
    }
    
    @Override
    public List<Map<String, Object>> getChatUsers(Integer adminId) {
        // 查询所有与管理员有聊天记录的用户
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> 
            wrapper.or(w -> w.eq(ChatMessage::getToUserId, adminId))  // 发给管理员的消息
                   .or(w -> w.eq(ChatMessage::getFromUserId, adminId)) // 管理员发出的消息
        );
        queryWrapper.orderByDesc(ChatMessage::getSendTime);
        
        List<ChatMessage> messages = this.list(queryWrapper);
        
        // 获取在线用户ID集合
        Set<Integer> onlineUserIds = ChatWebSocket.getOnlineUserIds();
        
        // 收集所有可能的用户ID（排除管理员自己）
        Set<Integer> potentialUserIds = new HashSet<>();
        for (ChatMessage msg : messages) {
            if (!msg.getFromUserId().equals(adminId)) {
                potentialUserIds.add(msg.getFromUserId());
            }
            if (msg.getToUserId() != null && msg.getToUserId() != 0 && !msg.getToUserId().equals(adminId)) {
                potentialUserIds.add(msg.getToUserId());
            }
        }
        
        // 过滤出真正的普通用户（在user表中存在的）
        List<Map<String, Object>> userList = new ArrayList<>();
        for (Integer userId : potentialUserIds) {
            // 跳过无效ID
            if (userId == null || userId == 0) {
                continue;
            }
            
            // 检查是否是普通用户（查询user表）
            User user = userService.getById(userId);
            if (user == null) {
                // 不是普通用户，可能是管理员，跳过
                continue;
            }
            
            // 找到该用户的所有消息
            List<ChatMessage> userMsgs = messages.stream()
                .filter(msg -> msg.getFromUserId().equals(userId) || 
                              (msg.getToUserId() != null && msg.getToUserId().equals(userId)))
                .collect(Collectors.toList());
            
            if (userMsgs.isEmpty()) {
                continue;
            }
            
            // 获取最后一条消息
            ChatMessage lastMessage = userMsgs.get(0);
            
            // 计算未读消息数（发给管理员但未读的）
            long unreadCount = userMsgs.stream()
                .filter(msg -> msg.getToUserId().equals(adminId) && msg.getIsRead() == 0)
                .count();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", userId);
            userInfo.put("name", user.getName()); // 使用user表中的名字
            userInfo.put("avatar", user.getAvatar()); // 添加头像
            userInfo.put("lastMessage", lastMessage.getContent());
            userInfo.put("lastMessageTime", lastMessage.getSendTime());
            userInfo.put("unreadCount", (int) unreadCount);
            userInfo.put("online", onlineUserIds.contains(userId)); // 根据WebSocket连接状态设置
            
            userList.add(userInfo);
        }
        
        // 按最后消息时间排序
        userList.sort((u1, u2) -> {
            LocalDateTime time1 = (LocalDateTime) u1.get("lastMessageTime");
            LocalDateTime time2 = (LocalDateTime) u2.get("lastMessageTime");
            return time2.compareTo(time1);
        });
        
        return userList;
    }
    
    @Override
    public void clearChatHistory(Integer userId, Integer otherUserId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果 otherUserId 为 0，表示清空该用户的所有聊天
        if (otherUserId == 0) {
            queryWrapper.and(wrapper -> 
                wrapper.or(w -> w.eq(ChatMessage::getFromUserId, userId))
                       .or(w -> w.eq(ChatMessage::getToUserId, userId))
            );
        } else {
            // 私聊模式：清空两个特定用户之间的聊天
            queryWrapper.and(wrapper -> 
                wrapper.or(w -> w.eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getToUserId, otherUserId))
                       .or(w -> w.eq(ChatMessage::getFromUserId, otherUserId).eq(ChatMessage::getToUserId, userId))
                       .or(w -> w.eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getToUserId, 0))
                       .or(w -> w.eq(ChatMessage::getFromUserId, otherUserId).eq(ChatMessage::getToUserId, 0))
            );
        }
        
        this.remove(queryWrapper);
    }
    
    /**
     * 填充消息中的用户头像
     */
    private void fillUserAvatars(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        // 获取所有不同的发送者ID
        Set<Integer> userIds = messages.stream()
            .map(ChatMessage::getFromUserId)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toSet());
        
        // 批量查询用户头像
        Map<Integer, String> avatarMap = new HashMap<>();
        for (Integer userId : userIds) {
            User user = userService.getById(userId);
            if (user != null) {
                avatarMap.put(userId, user.getAvatar());
            }
        }
        
        // 填充头像
        for (ChatMessage message : messages) {
            if (message.getFromUserId() != null && avatarMap.containsKey(message.getFromUserId())) {
                message.setFromUserAvatar(avatarMap.get(message.getFromUserId()));
            }
        }
    }
}
