package com.fullstackmall.controller;

import com.fullstackmall.dto.ApiResponse;
import com.fullstackmall.dto.AuthResponse;
import com.fullstackmall.dto.LoginRequest;
import com.fullstackmall.dto.RegisterRequest;
import com.fullstackmall.service.UserService;
import com.fullstackmall.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

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
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @param bindingResult 验证结果
     * @return 认证响应
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册接口，返回JWT token")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "注册成功",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误或用户已存在",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            BindingResult bindingResult) {
        
        // 检查验证错误
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "输入验证失败: " + errorMessage));
        }
        
        try {
            AuthResponse response = userService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "用户注册成功"));
        } catch (RuntimeException e) {
            // 区分不同类型的业务异常
            if (e.getMessage().contains("用户名已存在")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("USERNAME_EXISTS", e.getMessage()));
            } else if (e.getMessage().contains("邮箱已存在")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("EMAIL_EXISTS", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("REGISTRATION_ERROR", "注册失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"));
        }
    }
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @param bindingResult 验证结果
     * @return 认证响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT token")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "用户名或密码错误",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            BindingResult bindingResult) {
        
        // 检查验证错误
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "输入验证失败: " + errorMessage));
        }
        
        try {
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success(response, "登录成功"));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("INVALID_CREDENTIALS", "用户名或密码错误"));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("USER_NOT_FOUND", "用户不存在"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTHENTICATION_ERROR", "认证失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"));
        }
    }
    
    /**
     * 用户登出
     * @return 响应消息
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口（JWT无状态，前端删除token即可）")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登出成功",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT是无状态的，登出通常在前端处理（删除token）
        // 这里可以添加令牌黑名单逻辑（如果需要的话）
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }
}