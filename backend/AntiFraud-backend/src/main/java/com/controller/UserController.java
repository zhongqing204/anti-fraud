package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.dto.LoginDTO;
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
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(User user) {
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(User user,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = userService.selectPage(user, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        Integer userId = TokenUtils.getUserId(token);
        if (userId == null) {
            return Result.error("未登录");
        }
        User user = userService.getById(userId);
        return Result.success(user);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/updateCurrent")
    public Result updateCurrent(@RequestBody User user, HttpServletRequest request) {
        String token = request.getHeader("token");
        Integer userId = TokenUtils.getUserId(token);
        if (userId == null) {
            return Result.error("未登录");
        }
        user.setId(userId);
        user.setRole(null);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Map<String, Object> params) {
        userService.updatePassword(params);
        return Result.success();
    }
}
