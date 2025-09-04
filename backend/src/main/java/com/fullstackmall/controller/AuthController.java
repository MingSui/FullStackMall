package com.fullstackmall.controller;

import com.fullstackmall.dto.AuthResponse;
import com.fullstackmall.dto.LoginRequest;
import com.fullstackmall.dto.RegisterRequest;
import com.fullstackmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户注册、登录相关接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 认证响应
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册接口")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse response = userService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 认证响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = userService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 用户登出
     * @return 响应消息
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public ResponseEntity<?> logout() {
        // JWT是无状态的，登出通常在前端处理（删除token）
        return ResponseEntity.ok("登出成功");
    }
}