package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {
    void add(Message message);

    void deleteBatch(List<Integer> ids);

    List<Message> selectAll(Message message);

    Page<Message> selectPage(Message message, Integer pageNum, Integer pageSize);

    Integer getUnreadCount(Integer userId);

    void markAsRead(List<Integer> ids);

    void markAllAsRead(Integer userId);
}
