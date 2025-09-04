package com.fullstackmall.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录请求DTO
 */
@Schema(description = "用户登录请求对象")
public class LoginRequest {
    
    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "邮箱地址", example = "john@example.com", required = true)
    private String email;
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "password123", required = true)
    private String password;
    
    // 构造函数
    public LoginRequest() {}
    
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}