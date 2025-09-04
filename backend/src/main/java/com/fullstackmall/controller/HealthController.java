package com.fullstackmall.controller;

import com.fullstackmall.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/health")
@Tag(name = "健康检查", description = "系统健康检查接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {
    
    /**
     * 健康检查
     * @return 系统状态
     */
    @GetMapping
    @Operation(summary = "健康检查", description = "检查系统运行状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "FullStackMall Backend API",
            "version", "1.0.0"
        );
        
        return ResponseEntity.ok(ApiResponse.success(healthData, "系统运行正常"));
    }
    
    /**
     * API版本信息
     * @return 版本信息
     */
    @GetMapping("/version")
    @Operation(summary = "获取版本信息", description = "获取API版本信息")
    public ResponseEntity<ApiResponse<Map<String, String>>> version() {
        Map<String, String> versionData = Map.of(
            "version", "1.0.0",
            "apiVersion", "v1",
            "buildTime", "2024-01-15T10:30:00Z",
            "environment", "development"
        );
        
        return ResponseEntity.ok(ApiResponse.success(versionData, "版本信息"));
    }
}