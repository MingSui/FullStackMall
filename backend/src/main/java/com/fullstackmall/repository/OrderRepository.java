package com.fullstackmall.repository;

import com.fullstackmall.entity.Order;
import com.fullstackmall.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单数据访问接口
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 根据用户查找订单
     * @param user 用户
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByUser(User user, Pageable pageable);
    
    /**
     * 根据用户ID查找订单
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据订单状态查找订单
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    /**
     * 根据用户和订单状态查找订单
     * @param user 用户
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByUserAndStatus(User user, Order.OrderStatus status, Pageable pageable);
    
    /**
     * 根据创建时间范围查找订单
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * 统计用户订单数量
     * @param userId 用户ID
     * @return 订单数量
     */
    long countByUserId(Long userId);
    
    /**
     * 查找用户最近的订单
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 订单列表
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") Long userId, Pageable pageable);
}