package com.jackyblackson.idunntemplates.backend.interceptor;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 如果不是映射到方法（可能是静态资源等），直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 2. 检查是否有 @AuthRequired 注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        AuthRequired authRequired = handlerMethod.getMethodAnnotation(AuthRequired.class);

        if (authRequired == null) {
            return true;
        }

        String token = null;

        // 3. 优先尝试从 Authorization Header 中获取
        // 格式通常为: "Authorization: Bearer <token>"
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 去掉 "Bearer " 前缀
        }

        // 4. 如果 Header 中没有 Token，尝试从 Cookie 中获取 (兼容旧逻辑)
        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 5. 验证 Token
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            String uuid = jwtUtil.extractUuid(token);
            // 将用户信息放入 request，方便后续 Controller 使用
            request.setAttribute("userContext", new UserContext(username, uuid));
            return true;
        }

        // 6. 验证失败
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
