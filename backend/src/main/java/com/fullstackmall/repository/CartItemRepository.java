package com.fullstackmall.repository;

import com.fullstackmall.entity.Cart;
import com.fullstackmall.entity.CartItem;
import com.fullstackmall.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 购物车商品项数据访问接口
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * 根据购物车查找所有商品项
     * @param cart 购物车
     * @return 商品项列表
     */
    List<CartItem> findByCart(Cart cart);
    
    /**
     * 根据购物车ID查找所有商品项
     * @param cartId 购物车ID
     * @return 商品项列表
     */
    List<CartItem> findByCartId(Long cartId);
    
    /**
     * 根据购物车和商品查找商品项
     * @param cart 购物车
     * @param product 商品
     * @return 商品项
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    /**
     * 根据购物车ID和商品ID查找商品项
     * @param cartId 购物车ID
     * @param productId 商品ID
     * @return 商品项
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    /**
     * 删除购物车的所有商品项
     * @param cart 购物车
     */
    void deleteByCart(Cart cart);
}