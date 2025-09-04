package com.fullstackmall.controller;

import com.fullstackmall.dto.ApiResponse;
import com.fullstackmall.dto.CreateOrderRequest;
import com.fullstackmall.entity.Order;
import com.fullstackmall.entity.User;
import com.fullstackmall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "订单相关接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     * @param user 当前登录用户
     * @param request 创建订单请求
     * @param bindingResult 验证结果
     * @return 创建的订单
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "根据购物车内容或指定商品创建订单")
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request,
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
            Order order = orderService.createOrder(user, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "订单创建成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("商品不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("PRODUCT_NOT_FOUND", e.getMessage()));
            } else if (e.getMessage().contains("库存不足")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INSUFFICIENT_STOCK", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("CREATE_ORDER_ERROR", "创建订单失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("CREATE_ORDER_ERROR", "创建订单失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户的订单列表
     * @param user 当前登录用户
     * @param page 页码
     * @param size 每页大小
     * @return 用户订单列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的订单", description = "获取当前用户的订单列表")
    public ResponseEntity<ApiResponse<Page<Order>>> getMyOrders(
            @AuthenticationPrincipal User user,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Order> orders = orderService.findByUser(user, pageable);
            return ResponseEntity.ok(ApiResponse.success(orders, "获取订单列表成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("FETCH_ERROR", "获取订单列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取订单详情
     * @param user 当前登录用户
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "根据ID获取订单详情")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            @AuthenticationPrincipal User user,
            @Parameter(description = "订单ID") @PathVariable Long id) {
        
        try {
            Optional<Order> order = orderService.findById(id);
            if (order.isPresent()) {
                // 验证订单属于当前用户或者是管理员
                if (!order.get().getUser().getId().equals(user.getId()) && 
                    !user.getRole().equals(User.Role.ADMIN)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("PERMISSION_DENIED", "无权限查看此订单"));
                }
                return ResponseEntity.ok(ApiResponse.success(order.get(), "获取订单详情成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("ORDER_NOT_FOUND", "订单不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("FETCH_ERROR", "获取订单详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 取消订单
     * @param user 当前登录用户
     * @param id 订单ID
     * @return 取消的订单
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消指定的订单")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(
            @AuthenticationPrincipal User user,
            @Parameter(description = "订单ID") @PathVariable Long id) {
        
        try {
            Order cancelledOrder = orderService.cancelOrder(user, id);
            return ResponseEntity.ok(ApiResponse.success(cancelledOrder, "订单已取消"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("订单不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("ORDER_NOT_FOUND", e.getMessage()));
            } else if (e.getMessage().contains("无权限操作")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
            } else if (e.getMessage().contains("不允许取消")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATUS", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("CANCEL_ERROR", "取消订单失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("CANCEL_ERROR", "取消订单失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有订单（管理员功能）
     * @param page 页码
     * @param size 每页大小
     * @param status 订单状态过滤
     * @return 订单列表
     */
    @GetMapping("/admin/all")
    @Operation(summary = "获取所有订单", description = "获取所有订单（管理员权限）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Order>>> getAllOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "订单状态") @RequestParam(required = false) Order.OrderStatus status) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Order> orders;
            
            if (status != null) {
                orders = orderService.findByStatus(status, pageable);
            } else {
                orders = orderService.findAll(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(orders, "获取订单列表成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("FETCH_ERROR", "获取订单列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新订单状态（管理员功能）
     * @param id 订单ID
     * @param status 新状态
     * @return 更新的订单
     */
    @PutMapping("/admin/{id}/status")
    @Operation(summary = "更新订单状态", description = "更新订单状态（管理员权限）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam Order.OrderStatus status) {
        
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(updatedOrder, "订单状态已更新"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("订单不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("ORDER_NOT_FOUND", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPDATE_ERROR", "更新订单状态失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("UPDATE_ERROR", "更新订单状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取订单统计信息（管理员功能）
     * @return 订单统计
     */
    @GetMapping("/admin/statistics")
    @Operation(summary = "获取订单统计", description = "获取订单统计信息（管理员权限）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getOrderStatistics() {
        try {
            // 这里可以添加更多统计信息，如不同状态的订单数量等
            long totalOrders = orderService.findAll(Pageable.unpaged()).getTotalElements();
            
            return ResponseEntity.ok(ApiResponse.success(
                java.util.Map.of("totalOrders", totalOrders), 
                "获取订单统计成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("STATISTICS_ERROR", "获取订单统计失败: " + e.getMessage()));
        }
    }
}