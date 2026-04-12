package com.common.config;

import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.common.Constants;
import com.common.enums.ResultCode;
import com.common.exception.CustomException;
import com.entity.Admin;
import com.entity.User;
import com.service.AdminService;
import com.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 */
@Component
public class JWTInterceptor implements HandlerInterceptor {

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从 HTTP 请求头中获取 token
        String token = request.getHeader(Constants.TOKEN);

        // 如果请求头中没有，尝试从参数中获取
        if (ObjectUtil.isNull(token)) {
            token = request.getParameter(Constants.TOKEN);
        }

        // 2. 检查 token 是否存在
        if (ObjectUtil.isNull(token)) {
            throw new CustomException(ResultCode.TOKEN_INVALID_ERROR);
        }

        Object userObj = null;
        String password = null;

        try {
            // 3. 解析 token 获取用户信息
            String audience = JWT.decode(token).getAudience().get(0);
            String[] parts = audience.split("-");
            String userId = parts[0];
            String role = parts[1];

            // 4. 根据角色从对应的表查询用户信息
            if ("ADMIN".equals(role)) {
                Admin admin = adminService.getById(Integer.valueOf(userId));
                userObj = admin;
                if (admin != null) {
                    password = admin.getPassword();
                }
            } else if ("USER".equals(role)) {
                User user = userService.getById(Integer.valueOf(userId));
                userObj = user;
                if (user != null) {
                    password = user.getPassword();
                }
            } else {
                throw new CustomException(ResultCode.TOKEN_INVALID_ERROR);
            }
        }   catch (CustomException e){
                throw e;
        }   catch (Exception e) {
                throw new CustomException(ResultCode.TOKEN_CHECK_ERROR);
        }

        // 5. 检查用户是否存在
        if (ObjectUtil.isNull(userObj)) {
            throw new CustomException(ResultCode.TOKEN_CHECK_ERROR);
        }

        if (ObjectUtil.isNull(password)) {
            throw new CustomException(ResultCode.TOKEN_CHECK_ERROR);
        }

        try {
            // 6. 使用用户密码验证 token 的合法性
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(password)).build();
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new CustomException(ResultCode.TOKEN_CHECK_ERROR);
        }

        return true;
    }

}
