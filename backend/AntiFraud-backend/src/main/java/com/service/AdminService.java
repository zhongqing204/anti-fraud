package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Admin;

import java.util.List;
import java.util.Map;

public interface AdminService extends IService<Admin>{
    /**
     * 新增管理员
     */
    void add(Admin admin);

    /**
     * 批量删除管理员
     */
    void deleteBatch(List<Integer> ids);

    /**
     * 条件查询所有管理员
     */
    List<Admin> selectAll(Admin admin);

    /**
     * 分页查询管理员
     */
    Page<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize);

    /**
     * 管理员登录
     */
    Admin login(Admin admin);

    /**
     * 管理员修改密码
     */
    void updatePassword(Map<String, Object> params);

}

