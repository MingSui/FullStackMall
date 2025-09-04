package com.fullstackmall.repository;

import com.fullstackmall.entity.Cart;
import com.fullstackmall.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 购物车数据访问接口
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * 根据用户查找购物车
     * @param user 用户
     * @return 购物车对象
     */
    Optional<Cart> findByUser(User user);
    
    /**
     * 根据用户ID查找购物车
     * @param userId 用户ID
     * @return 购物车对象
     */
    Optional<Cart> findByUserId(Long userId);
}