package com.fullstackmall.service;

import com.fullstackmall.dto.CreateOrderRequest;
import com.fullstackmall.entity.*;
import com.fullstackmall.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 订单服务类
 */
@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartService cartService;
    
    /**
     * 创建订单
     * @param user 用户
     * @param request 创建订单请求
     * @return 创建的订单
     */
    public Order createOrder(User user, CreateOrderRequest request) {
        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 创建订单
        Order order = new Order(user, totalAmount, request.getShippingAddress());
        
        // 添加订单项并计算总金额
        for (CreateOrderRequest.OrderItemDto itemDto : request.getItems()) {
            Product product = productService.findById(itemDto.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在: " + itemDto.getProductId()));
            
            // 检查库存
            if (product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }
            
            // 创建订单项
            OrderItem orderItem = new OrderItem(order, product, itemDto.getQuantity(), product.getPrice());
            order.addItem(orderItem);
            
            // 累加总金额
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            
            // 减少库存
            productService.decreaseStock(product.getId(), itemDto.getQuantity());
        }
        
        // 设置总金额
        order.setTotalAmount(totalAmount);
        
        // 保存订单
        Order savedOrder = orderRepository.save(order);
        
        // 清空购物车
        cartService.clearCart(user);
        
        return savedOrder;
    }
    
    /**
     * 根据ID获取订单
     * @param id 订单ID
     * @return 订单对象
     */
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    /**
     * 获取用户的订单列表
     * @param user 用户
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    public Page<Order> findByUser(User user, Pageable pageable) {
        return orderRepository.findByUser(user, pageable);
    }
    
    /**
     * 获取所有订单（管理员功能）
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    /**
     * 根据状态查找订单
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    public Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
    
    /**
     * 更新订单状态（管理员功能）
     * @param orderId 订单ID
     * @param status 新状态
     * @return 更新的订单
     */
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
        
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        
        // 如果订单被取消，需要恢复库存
        if (status == Order.OrderStatus.CANCELLED && oldStatus != Order.OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * 取消订单
     * @param user 用户
     * @param orderId 订单ID
     * @return 取消的订单
     */
    public Order cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
        
        // 验证订单属于当前用户
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权限操作此订单");
        }
        
        // 只有待处理和已确认的订单可以取消
        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("当前订单状态不允许取消");
        }
        
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
    }
    
    /**
     * 恢复订单商品库存
     * @param order 订单
     */
    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            productService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }
    }
    
    /**
     * 统计用户订单数量
     * @param userId 用户ID
     * @return 订单数量
     */
    public long countByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }
}