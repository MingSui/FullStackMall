package com.fullstackmall.repository;

import com.fullstackmall.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品数据访问接口
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 根据分类查找商品
     * @param category 分类
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByCategory(String category, Pageable pageable);
    
    /**
     * 根据商品名称搜索商品（模糊查询）
     * @param name 商品名称关键词
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * 根据分类和名称搜索商品
     * @param category 分类
     * @param name 商品名称关键词
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByCategoryAndNameContainingIgnoreCase(String category, String name, Pageable pageable);
    
    /**
     * 查找库存大于0的商品
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByStockGreaterThan(Integer stock, Pageable pageable);
    
    /**
     * 获取所有分类
     * @return 分类列表
     */
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();
    
    /**
     * 多条件搜索商品
     * @param keyword 关键词（搜索名称和描述）
     * @param category 分类
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%) AND " +
           "(:category IS NULL OR p.category = :category)")
    Page<Product> findProducts(@Param("keyword") String keyword, 
                              @Param("category") String category, 
                              Pageable pageable);
}