package com.jackyblackson.idunntemplates.backend.interceptor;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.TrustedServerContext;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.domain.TrustedServer;
import com.jackyblackson.idunntemplates.backend.store.repository.TrustedServerRepository;
import com.jackyblackson.idunntemplates.backend.util.JwtUtil;
import com.jackyblackson.idunntemplates.core.IdunnConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final TrustedServerRepository trustedServerRepository;

    public AuthInterceptor(JwtUtil jwtUtil, TrustedServerRepository trustedServerRepository) {
        this.jwtUtil = jwtUtil;
        this.trustedServerRepository = trustedServerRepository;
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
        } else if (authHeader != null) {
            token = authHeader;
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

        // 5. 验证 Token (优先检查是否为普通 User JWT)
        boolean isValidJwt = false;
        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    isValidJwt = true;
                    String username = jwtUtil.extractUsername(token);
                    String uuid = jwtUtil.extractUuid(token);
                    // 将用户信息放入 request，方便后续 Controller 使用
                    request.setAttribute("userContext", new UserContext(username, uuid));
                    return true;
                }
            } catch (Exception ignored) {
                // 解析 JWT 异常（可能不是 JWT 格式而是 Server Token），继续后续流程
            }
        }

        // 6. 如果 JWT 验证失败（或非 JWT），且当前接口允许 Server Token 访问，则验证是否为有效的 Server Token
        if (!isValidJwt && authRequired.allowServerToken() && token != null) {
            Optional<TrustedServer> serverOpt = trustedServerRepository.findByToken(token);
            if (serverOpt.isPresent()) {
                TrustedServer server = serverOpt.get();
                request.setAttribute("trustedServerContext", new TrustedServerContext(server.getId(), server.getName()));
                request.setAttribute("userContext", new UserContext(IdunnConstants.INTERNAL_SUPER_USER_NAME, "00000000-0000-0000-0000-000000000000"));
                return true;
            }
        }

        // 7. 验证全部失败
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
