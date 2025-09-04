package com.fullstackmall.service;

import com.fullstackmall.dto.AuthResponse;
import com.fullstackmall.dto.LoginRequest;
import com.fullstackmall.dto.RegisterRequest;
import com.fullstackmall.entity.User;
import com.fullstackmall.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务类
 */
@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 认证响应
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(User.Role.USER);
        
        User savedUser = userRepository.save(user);
        
        // 生成JWT令牌
        String token = jwtService.generateToken(savedUser);
        
        return new AuthResponse(token, new AuthResponse.UserDto(savedUser));
    }
    
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
    
    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Spring Security UserDetailsService接口实现
     * @param email 邮箱（用作用户名）
     * @return UserDetails
     * @throws UsernameNotFoundException 用户未找到异常
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + email));
    }
}