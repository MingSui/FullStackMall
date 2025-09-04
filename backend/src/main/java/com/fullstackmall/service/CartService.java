package com.fullstackmall.service;

import com.fullstackmall.dto.AddToCartRequest;
import com.fullstackmall.entity.Cart;
import com.fullstackmall.entity.CartItem;
import com.fullstackmall.entity.Product;
import com.fullstackmall.entity.User;
import com.fullstackmall.repository.CartItemRepository;
import com.fullstackmall.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 购物车服务类
 */
@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductService productService;
    
    /**
     * 获取用户购物车
     * @param user 用户
     * @return 购物车
     */
    public Cart getOrCreateCart(User user) {
        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isPresent()) {
            return existingCart.get();
        } else {
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        }
    }
    
    /**
     * 获取购物车商品项
     * @param user 用户
     * @return 购物车商品项列表
     */
    public List<CartItem> getCartItems(User user) {
        Cart cart = getOrCreateCart(user);
        return cartItemRepository.findByCart(cart);
    }
    
    /**
     * 添加商品到购物车
     * @param user 用户
     * @param request 添加请求
     * @return 购物车商品项
     */
    public CartItem addToCart(User user, AddToCartRequest request) {
        // 获取或创建购物车
        Cart cart = getOrCreateCart(user);
        
        // 获取商品
        Product product = productService.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查库存
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("库存不足");
        }
        
        // 检查商品是否已在购物车中
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItem.isPresent()) {
            // 更新数量
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("库存不足");
            }
            
            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            // 创建新的购物车项
            CartItem newItem = new CartItem(cart, product, request.getQuantity());
            cart.addItem(newItem);
            return cartItemRepository.save(newItem);
        }
    }
    
    /**
     * 更新购物车商品数量
     * @param user 用户
     * @param itemId 商品项ID
     * @param quantity 新数量
     * @return 更新的购物车商品项
     */
    public CartItem updateCartItem(User user, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("购物车商品项不存在"));
        
        // 验证商品项属于当前用户
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权限操作此购物车商品项");
        }
        
        // 检查库存
        if (item.getProduct().getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }
    
    /**
     * 从购物车移除商品
     * @param user 用户
     * @param itemId 商品项ID
     */
    public void removeFromCart(User user, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("购物车商品项不存在"));
        
        // 验证商品项属于当前用户
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权限操作此购物车商品项");
        }
        
        Cart cart = item.getCart();
        cart.removeItem(item);
        cartItemRepository.delete(item);
    }
    
    /**
     * 清空购物车
     * @param user 用户
     */
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCart(cart);
        cart.clearItems();
        cartRepository.save(cart);
    }
    
    /**
     * 计算购物车总金额
     * @param user 用户
     * @return 总金额
     */
    public double calculateCartTotal(User user) {
        List<CartItem> items = getCartItems(user);
        return items.stream()
            .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
            .sum();
    }
}