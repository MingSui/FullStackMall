package com.fullstackmall.dto;

import com.fullstackmall.entity.User;

/**
 * 认证响应DTO
 */
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private UserDto user;
    
    // 构造函数
    public AuthResponse() {}
    
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
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private User.Role role;
        
        public UserDto() {}
        
        public UserDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.role = user.getRole();
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
    }
}