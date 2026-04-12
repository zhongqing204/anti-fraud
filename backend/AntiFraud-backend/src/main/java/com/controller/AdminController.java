package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Admin;
import com.entity.User;
import com.service.AdminService;
import com.service.UserService;
import com.utils.TokenUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private AdminService adminService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Admin admin) {
        adminService.add(admin);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Admin admin) {
        adminService.updateById(admin);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        adminService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        adminService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Admin admin = adminService.getById(id);
        return Result.success(admin);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Admin admin) {
        List<Admin> list = adminService.selectAll(admin);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Admin admin,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Admin> page = adminService.selectPage(admin, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/current")
    public Result getCurrentAdmin(HttpServletRequest request) {
        String token = request.getHeader("token");
        Integer adminId = TokenUtils.getUserId(token);
        if (adminId == null) {
            return Result.error("未登录");
        }
        Admin admin = adminService.getById(adminId);
        return Result.success(admin);
    }

    /**
     * 更新当前管理员信息
     */
    @PutMapping("/updateCurrent")
    public Result updateCurrent(@RequestBody Admin admin, HttpServletRequest request) {
        String token = request.getHeader("token");
        Integer adminId = TokenUtils.getUserId(token);
        if (adminId == null) {
            return Result.error("未登录");
        }
        admin.setId(adminId);
        admin.setRole(null);
        admin.setUpdateTime(LocalDateTime.now());
        adminService.updateById(admin);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Map<String, Object> params) {
        adminService.updatePassword(params);
        return Result.success();
    }


}
