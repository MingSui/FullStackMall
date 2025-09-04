package com.fullstackmall.controller;

import com.fullstackmall.dto.AddToCartRequest;
import com.fullstackmall.dto.ApiResponse;
import com.fullstackmall.entity.CartItem;
import com.fullstackmall.entity.User;
import com.fullstackmall.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "购物车管理", description = "购物车相关接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取用户购物车
     * 
     * @param user 当前登录用户
     * @return 购物车商品列表
     */
    @GetMapping
    @Operation(summary = "获取购物车", description = "获取当前用户的购物车商品列表")
    public ResponseEntity<ApiResponse<List<CartItem>>> getCart(
            @AuthenticationPrincipal User user) {

        try {
            List<CartItem> cartItems = cartService.getCartItems(user);
            return ResponseEntity.ok(ApiResponse.success(cartItems, "获取购物车成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("FETCH_ERROR", "获取购物车失败: " + e.getMessage()));
        }
    }

    /**
     * 添加商品到购物车
     * 
     * @param user          当前登录用户
     * @param request       添加请求
     * @param bindingResult 验证结果
     * @return 添加的购物车商品项
     */
    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车", description = "将商品添加到当前用户的购物车")
    public ResponseEntity<ApiResponse<CartItem>> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request,
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
            CartItem cartItem = cartService.addToCart(user, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(cartItem, "商品已添加到购物车"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("商品不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("PRODUCT_NOT_FOUND", e.getMessage()));
            } else if (e.getMessage().contains("库存不足")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("INSUFFICIENT_STOCK", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("ADD_TO_CART_ERROR", "添加到购物车失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("ADD_TO_CART_ERROR", "添加到购物车失败: " + e.getMessage()));
        }
    }

    /**
     * 更新购物车商品数量
     * 
     * @param user     当前登录用户
     * @param itemId   购物车商品项ID
     * @param quantity 新数量
     * @return 更新的购物车商品项
     */
    @PutMapping("/items/{itemId}")
    @Operation(summary = "更新购物车商品数量", description = "更新购物车中指定商品的数量")
    public ResponseEntity<ApiResponse<CartItem>> updateCartItem(
            @AuthenticationPrincipal User user,
            @Parameter(description = "购物车商品项ID") @PathVariable Long itemId,
            @Parameter(description = "新数量") @RequestParam Integer quantity) {

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_QUANTITY", "数量必须大于0"));
        }

        try {
            CartItem updatedItem = cartService.updateCartItem(user, itemId, quantity);
            return ResponseEntity.ok(ApiResponse.success(updatedItem, "购物车商品数量已更新"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("购物车商品项不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("CART_ITEM_NOT_FOUND", e.getMessage()));
            } else if (e.getMessage().contains("无权限操作")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
            } else if (e.getMessage().contains("库存不足")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("INSUFFICIENT_STOCK", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("UPDATE_ERROR", "更新购物车失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPDATE_ERROR", "更新购物车失败: " + e.getMessage()));
        }
    }

    /**
     * 从购物车移除商品
     * 
     * @param user   当前登录用户
     * @param itemId 购物车商品项ID
     * @return 移除结果
     */
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "移除购物车商品", description = "从购物车中移除指定商品")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @AuthenticationPrincipal User user,
            @Parameter(description = "购物车商品项ID") @PathVariable Long itemId) {

        try {
            cartService.removeFromCart(user, itemId);
            return ResponseEntity.ok(ApiResponse.success("商品已从购物车移除"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("购物车商品项不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("CART_ITEM_NOT_FOUND", e.getMessage()));
            } else if (e.getMessage().contains("无权限操作")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("REMOVE_ERROR", "移除商品失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("REMOVE_ERROR", "移除商品失败: " + e.getMessage()));
        }
    }

    /**
     * 清空购物车
     * 
     * @param user 当前登录用户
     * @return 清空结果
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车", description = "清空当前用户的购物车")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal User user) {

        try {
            cartService.clearCart(user);
            return ResponseEntity.ok(ApiResponse.success("购物车已清空"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("CLEAR_ERROR", "清空购物车失败: " + e.getMessage()));
        }
    }

    /**
     * 获取购物车统计信息
     * 
     * @param user 当前登录用户
     * @return 购物车统计信息
     */
    @GetMapping("/summary")
    @Operation(summary = "获取购物车统计", description = "获取购物车商品总数和总金额")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCartSummary(
            @AuthenticationPrincipal User user) {

        try {
            List<CartItem> cartItems = cartService.getCartItems(user);
            double totalAmount = cartService.calculateCartTotal(user);
            int totalItems = cartItems.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();

            Map<String, Object> summary = Map.of(
                    "totalItems", totalItems,
                    "totalAmount", totalAmount,
                    "itemCount", cartItems.size());

            return ResponseEntity.ok(ApiResponse.success(summary, "获取购物车统计成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SUMMARY_ERROR", "获取购物车统计失败: " + e.getMessage()));
        }
    }
}