package com.fullstackmall.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车实体类
 */
@Entity
@Table(name = "carts")
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Cart() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public Cart(User user) {
        this();
        this.user = user;
    }
    
    // 添加商品到购物车
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
        this.updatedAt = LocalDateTime.now();
    }
    
    // 从购物车移除商品
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        this.updatedAt = LocalDateTime.now();
    }
    
    // 清空购物车
    public void clearItems() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }
    
    // PreUpdate回调
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<CartItem> getItems() {
        return items;
    }
    
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}