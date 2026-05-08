package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Message;
import com.mapper.MessageMapper;
import com.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Override
    public void add(Message message) {
        message.setCreatedTime(LocalDateTime.now());
        message.setIsRead(0);
        this.save(message);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Message> selectAll(Message message) {
        LambdaQueryWrapper<Message> queryWrapper = buildQueryWrapper(message);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Message> selectPage(Message message, Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> queryWrapper = buildQueryWrapper(message);
        return this.page(page, queryWrapper);
    }

    @Override
    public Integer getUnreadCount(Integer userId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0);
        return (int) this.count(queryWrapper);
    }

    @Override
    public void markAsRead(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Message::getId, ids)
                .set(Message::getIsRead, 1);
        this.update(updateWrapper);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1);
        this.update(updateWrapper);
    }

    @Override
    public Page<Message> selectByType(Message message, Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> queryWrapper = buildQueryWrapper(message);
        // 按类型分组查询：like, collect, comment, report, article_report, activity_signup
        if (message != null && StringUtils.hasText(message.getType())) {
            queryWrapper.eq(Message::getType, message.getType());
        }
        return this.page(page, queryWrapper);
    }

    @Override
    public Integer getUnreadCountByType(Integer userId, String type) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .eq(Message::getType, type);
        return (int) this.count(queryWrapper);
    }

    private LambdaQueryWrapper<Message> buildQueryWrapper(Message message) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        if (message != null) {
            if (message.getUserId() != null) {
                queryWrapper.eq(Message::getUserId, message.getUserId());
            }
            if (StringUtils.hasText(message.getType())) {
                queryWrapper.eq(Message::getType, message.getType());
            }
            if (message.getIsRead() != null) {
                queryWrapper.eq(Message::getIsRead, message.getIsRead());
            }
        }
        queryWrapper.orderByDesc(Message::getCreatedTime);
        return queryWrapper;
    }
}
