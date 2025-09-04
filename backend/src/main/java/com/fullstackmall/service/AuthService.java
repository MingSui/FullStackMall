package com.fullstackmall.service;

import com.fullstackmall.dto.AuthResponse;
import com.fullstackmall.dto.LoginRequest;
import com.fullstackmall.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 认证服务类
 */
@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 认证响应
     */
    public AuthResponse login(LoginRequest loginRequest) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        
        User user = (User) authentication.getPrincipal();
        
        // 生成JWT令牌
        String token = jwtService.generateToken(user);
        
        return new AuthResponse(token, new AuthResponse.UserDto(user));
    }
}