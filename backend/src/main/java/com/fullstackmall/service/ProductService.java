package com.fullstackmall.service;

import com.fullstackmall.entity.Product;
import com.fullstackmall.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 商品服务类
 */
@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 获取所有商品（分页）
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    /**
     * 根据ID查找商品
     * @param id 商品ID
     * @return 商品对象
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * 搜索商品
     * @param keyword 关键词
     * @param category 分类
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    public Page<Product> searchProducts(String keyword, String category, Pageable pageable) {
        return productRepository.findProducts(keyword, category, pageable);
    }
    
    /**
     * 根据分类查找商品
     * @param category 分类
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    public Page<Product> findByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }
    
    /**
     * 根据名称搜索商品
     * @param name 商品名称关键词
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    
    /**
     * 获取有库存的商品
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    public Page<Product> findInStock(Pageable pageable) {
        return productRepository.findByStockGreaterThan(0, pageable);
    }
    
    /**
     * 获取所有分类
     * @return 分类列表
     */
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
    
    /**
     * 创建商品（管理员功能）
     * @param product 商品对象
     * @return 保存的商品
     */
    public Product save(Product product) {
        return productRepository.save(product);
    }
    
    /**
     * 更新商品（管理员功能）
     * @param id 商品ID
     * @param productDetails 商品详情
     * @return 更新的商品
     */
    public Product update(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在: " + id));
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategory(productDetails.getCategory());
        
        return productRepository.save(product);
    }
    
    /**
     * 删除商品（管理员功能）
     * @param id 商品ID
     */
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("商品不存在: " + id);
        }
        productRepository.deleteById(id);
    }
    
    /**
     * 减少商品库存
     * @param productId 商品ID
     * @param quantity 减少数量
     */
    public void decreaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
    
    /**
     * 增加商品库存
     * @param productId 商品ID
     * @param quantity 增加数量
     */
    public void increaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));
        
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }
}