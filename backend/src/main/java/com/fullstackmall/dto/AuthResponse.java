package com.fullstackmall.dto;

import com.fullstackmall.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 认证响应DTO
 */
@Schema(description = "认证响应对象")
public class AuthResponse {

    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Token类型", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "用户信息")
    private UserDto user;

    // 构造函数
    public AuthResponse() {
    }

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    /**
     * 用户信息DTO
     */
    @Schema(description = "用户信息对象")
    public static class UserDto {
        @Schema(description = "用户ID", example = "1")
        private Long id;

        @Schema(description = "用户名", example = "john_doe")
        private String username;

        @Schema(description = "邮箱地址", example = "john@example.com")
        private String email;

        @Schema(description = "用户角色", example = "USER")
        private User.Role role;

        @Schema(description = "注册时间", example = "2023-12-01T10:30:00")
        private LocalDateTime createdAt;

        public UserDto() {
        }

        public UserDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.role = user.getRole();
            this.createdAt = user.getCreatedAt();
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public User.Role getRole() {
            return role;
        }

        public void setRole(User.Role role) {
            this.role = role;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}