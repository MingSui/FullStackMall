package com.fullstackmall.repository;

import com.fullstackmall.entity.Order;
import com.fullstackmall.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单商品项数据访问接口
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * 根据订单查找所有商品项
     * @param order 订单
     * @return 商品项列表
     */
    List<OrderItem> findByOrder(Order order);
    
    /**
     * 根据订单ID查找所有商品项
     * @param orderId 订单ID
     * @return 商品项列表
     */
    List<OrderItem> findByOrderId(Long orderId);
}