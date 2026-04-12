package com.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.enums.ResultCode;
import com.common.exception.CustomException;
import com.entity.Admin;
import com.mapper.AdminMapper;
import com.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public void add(Admin admin) {
        // 1. 校验账号是否已存在
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", admin.getAccount());
        Admin existAdmin = adminMapper.selectOne(queryWrapper);
        if (existAdmin != null) {
            throw new CustomException(ResultCode.USER_EXIST_ERROR);
        }

        // 2. 密码加密（MD5）
        String password = admin.getPassword();
        if (password == null || password.isEmpty()) {
            password = "123456";
        }
        admin.setPassword(DigestUtil.md5Hex(password));

        // 3. 设置时间字段
        LocalDateTime now = LocalDateTime.now();
        admin.setCreateTime(now);
        admin.setUpdateTime(now);

        // 4. 自动设置角色为管理员
        admin.setRole("管理员");

        // 5. 插入数据
        adminMapper.insert(admin);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException(ResultCode.PARAM_ERROR, "删除ID列表不能为空");
        }
        adminMapper.deleteBatchIds(ids);
    }

    @Override
    public List<Admin> selectAll(Admin admin) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        if (admin != null) {
            // 模糊查询姓名
            if (StringUtils.hasText(admin.getName())) {
                queryWrapper.like("name", admin.getName());
            }
            // 精确查询账号
            if (StringUtils.hasText(admin.getAccount())) {
                queryWrapper.eq("account", admin.getAccount());
            }
            // 精确查询角色
            if (StringUtils.hasText(admin.getRole())) {
                queryWrapper.eq("role", admin.getRole());
            }
        }
        // 按更新时间倒序
        queryWrapper.orderByDesc("update_time");
        return adminMapper.selectList(queryWrapper);
    }


    @Override
    public Page<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        // 校验分页参数
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        Page<Admin> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();

        // 条件查询（只添加非空条件）
        if (admin != null) {
            if (StringUtils.hasText(admin.getAccount())) {
                queryWrapper.like("account", admin.getAccount());
            }
            if (StringUtils.hasText(admin.getName())) {
                queryWrapper.like("name", admin.getName());
            }
            if (StringUtils.hasText(admin.getRole())) {
                queryWrapper.eq("role", admin.getRole());
            }
        }

        // 只按更新时间倒序，避免重复排序
        queryWrapper.orderByDesc("update_time");
        return adminMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Admin login(Admin admin) {
        // 1. 校验参数
        if (!StringUtils.hasText(admin.getAccount()) || !StringUtils.hasText(admin.getPassword())) {
            throw new CustomException(ResultCode.PARAM_ERROR, "账号或密码不能为空");
        }

        // 2. 查询管理员信息
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", admin.getAccount());
        Admin dbAdmin = adminMapper.selectOne(queryWrapper);

        // 3. 校验账号是否存在
        if (dbAdmin == null) {
            throw new CustomException(ResultCode.USER_NOT_EXIST_ERROR);
        }

        // 4. 校验密码（MD5对比）
        String encryptPwd = DigestUtil.md5Hex(admin.getPassword());
        if (!encryptPwd.equals(dbAdmin.getPassword())) {
            throw new CustomException(ResultCode.USER_ACCOUNT_ERROR);
        }

        dbAdmin.setRole("ADMIN");

        return dbAdmin;
    }

    @Override
    public void updatePassword(Map<String, Object> params) {
        // 1. 获取参数
        Object idObj = params.get("id");
        String newPassword = (String) params.get("newPassword");

        // 2. 校验参数
        if (idObj == null || newPassword == null || newPassword.isEmpty()) {
            throw new CustomException(ResultCode.PARAM_ERROR, "管理员 ID 和新密码不能为空");
        }

        Integer adminId;
        if (idObj instanceof Integer) {
            adminId = (Integer) idObj;
        } else {
            adminId = Integer.parseInt(idObj.toString());
        }

        // 3. 校验管理员是否存在
        Admin dbAdmin = adminMapper.selectById(adminId);
        if (dbAdmin == null) {
            throw new CustomException(ResultCode.USER_NOT_EXIST_ERROR);
        }

        // 4. 密码加密并更新
        dbAdmin.setPassword(DigestUtil.md5Hex(newPassword));
        dbAdmin.setUpdateTime(LocalDateTime.now());
        adminMapper.updateById(dbAdmin);
    }
}
