package com.common.config;

import cn.hutool.json.JSONUtil;
import com.entity.ChatMessage;
import com.service.ChatMessageService;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket聊天服务端点
 */
@ServerEndpoint("/chat/{userId}")
@Component
public class ChatWebSocket {
    
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocket.class);
    
    /**
     * 存储所有在线用户的WebSocket会话
     * Key: userId, Value: Session列表（支持同一用户多端登录）
     */
    private static ConcurrentHashMap<Integer, java.util.List<Session>> onlineUsers = new ConcurrentHashMap<>();
    
    private static ChatMessageService chatMessageService;
    
    @Resource
    public void setChatMessageService(ChatMessageService chatMessageService) {
        ChatWebSocket.chatMessageService = chatMessageService;
    }
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        // 添加用户的Session到列表
        onlineUsers.computeIfAbsent(userId, k -> new java.util.ArrayList<>()).add(session);
        log.info("用户{}已连接，当前在线人数:{}", userId, onlineUsers.size());
        
        // 发送欢迎消息
        sendMessageToUser(userId, new ChatMessageDTO(0, "系统", null, "欢迎使用反诈平台在线咨询", "system", LocalDateTime.now()));
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Integer userId) {
        // 移除当前关闭的Session
        java.util.List<Session> sessions = onlineUsers.get(userId);
        if (sessions != null) {
            // 直接移除当前session，不检查isOpen状态
            sessions.remove(session);
            log.info("用户{}的一个Session已关闭，剩余Session数:{}", userId, sessions.size());
            
            // 如果该用户没有Session了，从在线列表中移除
            if (sessions.isEmpty()) {
                onlineUsers.remove(userId);
                log.info("用户{}的所有Session已关闭，从在线列表移除", userId);
            }
        }
        log.info("用户{}已断开连接，当前在线人数:{}", userId, onlineUsers.size());
    }
    
    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Integer fromUserId) {
        log.info("收到用户{}的消息: {}", fromUserId, message);
        
        try {
            ChatMessageDTO msgDTO = JSONUtil.toBean(message, ChatMessageDTO.class);
            
            // 构建聊天记录对象
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setFromUserId(fromUserId);
            chatMessage.setFromUserName(msgDTO.getFromUserName());
            chatMessage.setFromUserAvatar(msgDTO.getFromUserAvatar()); // 保存头像
            chatMessage.setToUserId(msgDTO.getToUserId());
            chatMessage.setToUserName(msgDTO.getToUserName());
            chatMessage.setContent(msgDTO.getContent());
            chatMessage.setMessageType(msgDTO.getMessageType() != null ? msgDTO.getMessageType() : "text");
            chatMessage.setIsRead(0);
            chatMessage.setSendTime(LocalDateTime.now());
            
            // 保存到数据库
            chatMessageService.save(chatMessage);
            
            // 发送给接收者
            Integer toUserId = msgDTO.getToUserId();
            log.info("消息类型: toUserId={}, fromUserId={}", toUserId, fromUserId);
            log.info("当前在线用户: {}", onlineUsers.keySet());
            
            if (toUserId != null && toUserId > 0) {
                // 私聊：发送给指定用户的所有Session
                log.info("私聊模式：发送给 userId={}", toUserId);
                sendMessageToUser(toUserId, new ChatMessageDTO(
                    fromUserId, 
                    msgDTO.getFromUserName(),
                    msgDTO.getFromUserAvatar(), // 添加头像信息
                    msgDTO.getContent(), 
                    msgDTO.getMessageType(),
                    chatMessage.getSendTime()
                ));
            } else {
                // 群聊/广播：发送给所有在线用户（除发送者外）
                log.info("广播模式：发送给所有在线用户，排除 userId={}", fromUserId);
                broadcastMessage(fromUserId, new ChatMessageDTO(
                    fromUserId,
                    msgDTO.getFromUserName(),
                    msgDTO.getFromUserAvatar(), // 添加头像信息
                    msgDTO.getContent(),
                    msgDTO.getMessageType(),
                    chatMessage.getSendTime()
                ));
            }
            
            // 回发给发送者确认（发送到发送者的所有Session）
            sendMessageToUser(fromUserId, new ChatMessageDTO(
                fromUserId,
                msgDTO.getFromUserName(),
                msgDTO.getFromUserAvatar(), // 添加头像信息
                msgDTO.getContent(),
                msgDTO.getMessageType(),
                chatMessage.getSendTime(),
                true // 标记为自己发送的消息
            ));
            
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }
    
    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Integer userId) {
        log.error("用户{}的WebSocket发生错误: {}", userId, error.getMessage());
        error.printStackTrace();
    }
    
    /**
     * 发送消息给指定用户（所有Session）
     */
    private void sendMessageToUser(Integer userId, ChatMessageDTO message) {
        java.util.List<Session> sessions = onlineUsers.get(userId);
        log.info("尝试发送消息给用户 userId={}, sessions数量={}", userId, sessions != null ? sessions.size() : 0);
        
        if (sessions != null) {
            // 先清理已关闭的Session
            sessions.removeIf(session -> !session.isOpen());
            
            int successCount = 0;
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(JSONUtil.toJsonStr(message));
                        successCount++;
                        log.info("消息发送成功给用户 userId={}", userId);
                    } catch (IOException e) {
                        log.error("发送消息给用户{}失败", userId, e);
                    }
                }
            }
            log.info("实际发送成功数: {}/{}", successCount, sessions.size());
            
            // 如果清理后没有Session了，从在线列表中移除
            if (sessions.isEmpty()) {
                onlineUsers.remove(userId);
                log.info("用户{}的所有Session已失效，从在线列表移除", userId);
            }
        } else {
            log.warn("用户不在线，userId={}", userId);
        }
    }
    
    /**
     * 广播消息给所有在线用户（排除发送者）
     */
    private void broadcastMessage(Integer excludeUserId, ChatMessageDTO message) {
        log.info("开始广播消息，排除 userId={}", excludeUserId);
        java.util.concurrent.atomic.AtomicInteger totalSent = new java.util.concurrent.atomic.AtomicInteger(0);
        
        onlineUsers.forEach((userId, sessions) -> {
            if (!userId.equals(excludeUserId)) {
                // 先清理已关闭的Session
                sessions.removeIf(session -> !session.isOpen());
                
                if (sessions.isEmpty()) {
                    log.info("用户{}没有可用的Session，跳过", userId);
                    return;
                }
                
                log.info("广播给用户 userId={}, sessions数量={}", userId, sessions.size());
                for (Session session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.getBasicRemote().sendText(JSONUtil.toJsonStr(message));
                            totalSent.incrementAndGet();
                            log.info("广播成功给用户 userId={}", userId);
                        } catch (IOException e) {
                            log.error("广播消息给用户{}失败", userId, e);
                        }
                    }
                }
            } else {
                log.info("跳过发送者 userId={}", userId);
            }
        });
        
        log.info("广播完成，总共发送给 {} 个Session", totalSent.get());
    }
    
    /**
     * 获取在线用户列表
     */
    public static java.util.Set<Integer> getOnlineUserIds() {
        return onlineUsers.keySet();
    }
    
    /**
     * 聊天消息DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ChatMessageDTO {
        private Integer fromUserId;
        private String fromUserName;
        private String fromUserAvatar;
        private Integer toUserId;
        private String toUserName;
        private String content;
        private String messageType;
        private LocalDateTime sendTime;
        private Boolean isSelf; // 是否是自己发送的消息
        
        public ChatMessageDTO(Integer fromUserId, String fromUserName, String fromUserAvatar, String content, String messageType, LocalDateTime sendTime) {
            this.fromUserId = fromUserId;
            this.fromUserName = fromUserName;
            this.fromUserAvatar = fromUserAvatar;
            this.toUserId = 0;
            this.toUserName = "";
            this.content = content;
            this.messageType = messageType;
            this.sendTime = sendTime;
            this.isSelf = false;
        }
        
        public ChatMessageDTO(Integer fromUserId, String fromUserName, String fromUserAvatar, Integer toUserId, String toUserName, String content, String messageType, LocalDateTime sendTime) {
            this.fromUserId = fromUserId;
            this.fromUserName = fromUserName;
            this.fromUserAvatar = fromUserAvatar;
            this.toUserId = toUserId;
            this.toUserName = toUserName;
            this.content = content;
            this.messageType = messageType;
            this.sendTime = sendTime;
            this.isSelf = false;
        }
        
        public ChatMessageDTO(Integer fromUserId, String fromUserName, String fromUserAvatar, String content, String messageType, LocalDateTime sendTime, Boolean isSelf) {
            this.fromUserId = fromUserId;
            this.fromUserName = fromUserName;
            this.fromUserAvatar = fromUserAvatar;
            this.toUserId = 0;
            this.toUserName = "";
            this.content = content;
            this.messageType = messageType;
            this.sendTime = sendTime;
            this.isSelf = isSelf;
        }
    }
}
