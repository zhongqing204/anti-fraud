package com.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.common.Constants;
import com.entity.Admin;
import com.entity.User;
import com.service.AdminService;
import com.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.Map;

/**
 * Token工具类
 */
@Component
public class TokenUtils {
    private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    private static AdminService staticAdminService;
    private static UserService staticUserService;

    @PostConstruct
    public void init() {
        staticAdminService = adminService;
        staticUserService = userService;
    }

    /**
     * 生成JWT token
     */
    public static String createToken(String data, String sign) {
        return JWT.create().withAudience(data)
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1))
                .sign(Algorithm.HMAC256(sign));
    }

    /**
     * 生成 JWT token
     * @param userId 用户 ID
     * @param role 用户角色（ADMIN 或 USER）
     * @param password 用户密码（用于签名）
     */
    public static String createToken(String userId, String role, String password) {
        String audience = userId + "-" + role;
        return JWT.create().withAudience(audience)
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1))
                .sign(Algorithm.HMAC256(password));
    }

    /**
     * 获取当前登录的用户
     */
    public static Map<String, Object> getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader(Constants.TOKEN);

            if (token == null) {
                return null;
            }

            String audience = JWT.decode(token).getAudience().get(0);
            String[] parts = audience.split("-");
            Integer userId = Integer.valueOf(parts[0]);
            String role = parts[1];

            if ("ADMIN".equals(role)) {
                Admin admin = staticAdminService.getById(userId);
                if (admin != null) {
                    return Map.of(
                            "id", admin.getId(),
                            "name", admin.getName(),
                            "account", admin.getAccount(),
                            "role", admin.getRole(),
                            "avatar", admin.getAvatar()
                    );
                }
            } else if ("USER".equals(role)) {
                User user = staticUserService.getById(userId);
                if (user != null) {
                    return Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "account", user.getAccount(),
                            "role", user.getRole(),
                            "avatar", user.getAvatar()
                    );
                }
            }
        } catch (Exception e) {
            log.error("获取当前登录用户出错", e);
        }
        return null;
    }

    /**
     * 从 token 中获取用户 ID
     */
    public static Integer getUserId(String token) {
        try {
            if (token == null) {
                return null;
            }
            String audience = JWT.decode(token).getAudience().get(0);
            String[] parts = audience.split("-");
            return Integer.valueOf(parts[0]);
        } catch (Exception e) {
            log.error("解析 token 中的用户 ID 出错", e);
            return null;
        }
    }

    /**
     * 从 token 中获取用户角色
     */
    public static String getRole(String token) {
        try {
            if (token == null) {
                return null;
            }
            String audience = JWT.decode(token).getAudience().get(0);
            String[] parts = audience.split("-");
            return parts[1];
        } catch (Exception e) {
            log.error("解析 token 中的用户角色出错", e);
            return null;
        }
    }
}
