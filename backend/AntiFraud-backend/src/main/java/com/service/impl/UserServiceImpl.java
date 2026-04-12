package com.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.enums.ResultCode;
import com.common.exception.CustomException;
import com.entity.User;
import com.mapper.UserMapper;
import com.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public void add(User user) {
        // 1. 校验账号是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", user.getAccount());
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new CustomException(ResultCode.USER_EXIST_ERROR);
        }

        // 2. 密码加密（MD5）
        String password = user.getPassword();
        if (password == null || password.isEmpty()) {
            password = "123456";
        }
        user.setPassword(DigestUtil.md5Hex(password));

        // 3. 设置时间字段
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        // 4. 自动设置角色为用户
        user.setRole("用户");

        // 5. 插入数据
        userMapper.insert(user);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException(ResultCode.PARAM_ERROR, "删除ID列表不能为空");
        }
        userMapper.deleteBatchIds(ids);
    }

    @Override
    public List<User> selectAll(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (user != null) {
            // 模糊查询姓名
            if (StringUtils.hasText(user.getName())) {
                queryWrapper.like("name", user.getName());
            }
            // 精确查询账号
            if (StringUtils.hasText(user.getAccount())) {
                queryWrapper.eq("account", user.getAccount());
            }
        }
        // 按更新时间倒序
        queryWrapper.orderByDesc("update_time");
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public Page<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        // 校验分页参数
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 条件查询
        if (user != null){
            if (StringUtils.hasText(user.getAccount())) {
                queryWrapper.like("account", user.getAccount());
            }
            if (StringUtils.hasText(user.getName())) {
                queryWrapper.like("name", user.getName());
            }
            if (StringUtils.hasText(user.getRole())) {
                queryWrapper.eq("role", user.getRole());
            }
        }

        queryWrapper.orderByDesc("update_time");
        return userMapper.selectPage(page, queryWrapper);
    }

    @Override
    public User login(User user) {
        // 1. 校验参数
        if (!StringUtils.hasText(user.getAccount()) || !StringUtils.hasText(user.getPassword())) {
            throw new CustomException(ResultCode.PARAM_ERROR, "账号或密码不能为空");
        }

        // 2. 查询用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", user.getAccount());
        User dbUser = userMapper.selectOne(queryWrapper);

        // 3. 校验账号是否存在
        if (dbUser == null) {
            throw new CustomException(ResultCode.USER_NOT_EXIST_ERROR);
        }

        // 4. 校验密码（MD5对比）
        String encryptPwd = DigestUtil.md5Hex(user.getPassword());
        if (!encryptPwd.equals(dbUser.getPassword())) {
            throw new CustomException(ResultCode.USER_ACCOUNT_ERROR);
        }

        dbUser.setRole("USER");

        return dbUser;
    }

    @Override
    public void updatePassword(Map<String, Object> params) {
        // 1. 获取参数
        Object idObj = params.get("id");
        String newPassword = (String) params.get("newPassword");

        // 2. 校验参数
        if (idObj == null || newPassword == null || newPassword.isEmpty()) {
            throw new CustomException(ResultCode.PARAM_ERROR, "用户 ID 和新密码不能为空");
        }

        Integer userId;
        if (idObj instanceof Integer) {
            userId = (Integer) idObj;
        } else {
            userId = Integer.parseInt(idObj.toString());
        }

        // 3. 校验用户是否存在
        User dbUser = userMapper.selectById(userId);
        if (dbUser == null) {
            throw new CustomException(ResultCode.USER_NOT_EXIST_ERROR);
        }

        // 4. 密码加密并更新
        dbUser.setPassword(DigestUtil.md5Hex(newPassword));
        dbUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(dbUser);
    }
}
