package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.LoginRequest;
import com.jackyblackson.idunntemplates.backend.dto.LoginResponseDto;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.YggdrasilService;
import com.jackyblackson.idunntemplates.backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final YggdrasilService yggdrasilService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Autowired
    public AuthController(YggdrasilService yggdrasilService, JwtUtil jwtUtil) {
        this.yggdrasilService = yggdrasilService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        UserContext user = yggdrasilService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            String token = jwtUtil.generateToken(user);
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            // cookie.setSecure(true); // Enable in production with HTTPS
            cookie.setMaxAge((int) jwtExpiration);
            response.addCookie(cookie);
            return ResponseEntity.ok(LoginResponseDto.fromUserContext(
                    user,
                    token,
                    jwtExpiration
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or authentication failed");
        }
    }

    @GetMapping("/me")
    @AuthRequired
    public ResponseEntity<UserContext> getCurrentUser(UserContext user) {
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 创建一个同名的 Cookie
        Cookie cookie = new Cookie("auth_token", null);

        // 关键设置：Path 必须与登录时设置的完全一致（通常是 "/"）
        cookie.setPath("/");

        // 设为 HttpOnly 保持一致性
        cookie.setHttpOnly(true);

        // 将有效期设置为 0，指令浏览器立即删除该 Cookie
        cookie.setMaxAge(0);

        // 如果生产环境开启了 Secure，这里也建议保持一致
        // cookie.setSecure(true);

        // 将该 Cookie 写入响应头
        response.addCookie(cookie);

        return ResponseEntity.ok("Successfully logged out");
    }
}
