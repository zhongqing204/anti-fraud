package com.controller;

import com.common.Result;
import com.dto.LoginDTO;
import com.dto.UserRegisterDTO;
import com.entity.Admin;
import com.entity.User;
import com.service.AdminService;
import com.service.UserService;
import com.utils.TokenUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource
    private UserService userService;

    @Resource
    private AdminService adminService;

    /**
     * 统一登录接口（支持用户和管理员）
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, Object> loginData) {
        String account = loginData.get("account").toString();
        String password = loginData.get("password").toString();
        String role = loginData.get("role").toString();

        Object userInfo = null;
        String token = null;
        if ("ADMIN".equals(role)) {
            // 管理员登录
            Admin admin = new Admin();
            admin.setAccount(account);
            admin.setPassword(password);
            Admin loginAdmin = adminService.login(admin);
            token = TokenUtils.createToken(String.valueOf(loginAdmin.getId()), "ADMIN", loginAdmin.getPassword());
            loginAdmin.setRole("ADMIN");
            userInfo = loginAdmin;
        } else {
            // 用户登录
            User user = new User();
            user.setAccount(account);
            user.setPassword(password);
            User loginUser = userService.login(user);
            token = TokenUtils.createToken(String.valueOf(loginUser.getId()), "USER", loginUser.getPassword());
            loginUser.setRole("USER");
            userInfo = loginUser;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        // 校验两次密码是否一致（前端已校验，后端再次校验）
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.error("两次输入的密码不一致");
        }

        // 构建用户对象
        User user = new User();
        user.setAccount(registerDTO.getAccount());
        user.setPassword(registerDTO.getPassword());

        // 调用服务层注册
        userService.add(user);
        return Result.success();
    }

}
