package com.fullstackmall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 创建订单请求DTO
 */
public class CreateOrderRequest {
    
    @NotBlank(message = "收货地址不能为空")
    private String shippingAddress;
    
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemDto> items;
    
    // 构造函数
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(String shippingAddress, List<OrderItemDto> items) {
        this.shippingAddress = shippingAddress;
        this.items = items;
    }
    
    // Getters and Setters
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public List<OrderItemDto> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
    
    /**
     * 订单商品项DTO
     */
    public static class OrderItemDto {
        private Long productId;
        private Integer quantity;
        
        public OrderItemDto() {}
        
        public OrderItemDto(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        // Getters and Setters
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}