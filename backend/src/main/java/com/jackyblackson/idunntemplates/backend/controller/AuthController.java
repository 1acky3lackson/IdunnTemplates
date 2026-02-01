package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.LoginRequest;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.YggdrasilService;
import com.jackyblackson.idunntemplates.backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final YggdrasilService yggdrasilService;
    private final JwtUtil jwtUtil;

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
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or authentication failed");
        }
    }

    @GetMapping("/me")
    @AuthRequired
    public ResponseEntity<UserContext> getCurrentUser(UserContext user) {
        return ResponseEntity.ok(user);
    }
}
