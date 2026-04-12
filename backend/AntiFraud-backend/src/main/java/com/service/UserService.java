package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {
    void add(User user);

    void deleteBatch(List<Integer> ids);

    List<User> selectAll(User user);

    Page<User> selectPage(User user, Integer pageNum, Integer pageSize);

    User login(User user);

    void updatePassword(Map<String, Object> params);

}
