package com.fullstackmall.controller;

import com.fullstackmall.dto.ApiResponse;
import com.fullstackmall.entity.Product;
import com.fullstackmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "商品管理", description = "商品相关接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * 获取商品列表（分页）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序字段
     * @param direction 排序方向
     * @return 商品分页列表
     */
    @GetMapping
    @Operation(summary = "获取商品列表", description = "分页获取商品列表")
    public ResponseEntity<ApiResponse<Page<Product>>> getProducts(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "ASC") String direction) {
        
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<Product> products = productService.findAll(pageable);
            
            return ResponseEntity.ok(ApiResponse.success(products, "获取商品列表成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("FETCH_ERROR", "获取商品列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据ID获取商品详情")
    public ResponseEntity<ApiResponse<Product>> getProduct(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        
        try {
            Optional<Product> product = productService.findById(id);
            if (product.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(product.get(), "获取商品详情成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("PRODUCT_NOT_FOUND", "商品不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("FETCH_ERROR", "获取商品详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索商品
     * @param keyword 关键词
     * @param category 分类
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据关键词和分类搜索商品")
    public ResponseEntity<ApiResponse<Page<Product>>> searchProducts(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商品分类") @RequestParam(required = false) String category,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products;
            
            if (keyword != null && !keyword.isEmpty()) {
                products = productService.searchProducts(keyword, category, pageable);
            } else if (category != null && !category.isEmpty()) {
                products = productService.findByCategory(category, pageable);
            } else {
                products = productService.findInStock(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(products, "搜索商品成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SEARCH_ERROR", "搜索商品失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有商品分类
     * @return 分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取商品分类", description = "获取所有商品分类列表")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        try {
            List<String> categories = productService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories, "获取分类列表成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("FETCH_ERROR", "获取分类列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建商品（管理员权限）
     * @param product 商品信息
     * @param bindingResult 验证结果
     * @return 创建的商品
     */
    @PostMapping
    @Operation(summary = "创建商品", description = "创建新商品（需要管理员权限）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @Valid @RequestBody Product product,
            BindingResult bindingResult) {
        
        // 检查验证错误
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "输入验证失败: " + errorMessage));
        }
        
        try {
            Product savedProduct = productService.save(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedProduct, "商品创建成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("CREATE_ERROR", "创建商品失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新商品（管理员权限）
     * @param id 商品ID
     * @param product 商品信息
     * @param bindingResult 验证结果
     * @return 更新的商品
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商品", description = "更新商品信息（需要管理员权限）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Valid @RequestBody Product product,
            BindingResult bindingResult) {
        
        // 检查验证错误
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "输入验证失败: " + errorMessage));
        }
        
        try {
            Product updatedProduct = productService.update(id, product);
            return ResponseEntity.ok(ApiResponse.success(updatedProduct, "商品更新成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("商品不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("PRODUCT_NOT_FOUND", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPDATE_ERROR", "更新商品失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("UPDATE_ERROR", "更新商品失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除商品（管理员权限）
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "删除商品（需要管理员权限）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        
        try {
            productService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("商品删除成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("商品不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("PRODUCT_NOT_FOUND", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DELETE_ERROR", "删除商品失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DELETE_ERROR", "删除商品失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新商品库存（管理员权限）
     * @param id 商品ID
     * @param quantity 库存数量
     * @param operation 操作类型（increase/decrease）
     * @return 更新结果
     */
    @PatchMapping("/{id}/stock")
    @Operation(summary = "更新商品库存", description = "增加或减少商品库存（需要管理员权限）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "库存数量") @RequestParam Integer quantity,
            @Parameter(description = "操作类型") @RequestParam(defaultValue = "increase") String operation) {
        
        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_QUANTITY", "数量必须大于0"));
        }
        
        try {
            if ("increase".equals(operation)) {
                productService.increaseStock(id, quantity);
            } else if ("decrease".equals(operation)) {
                productService.decreaseStock(id, quantity);
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_OPERATION", "无效的操作类型"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("库存更新成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("商品不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("PRODUCT_NOT_FOUND", e.getMessage()));
            } else if (e.getMessage().contains("库存不足")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INSUFFICIENT_STOCK", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("STOCK_UPDATE_ERROR", "更新库存失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("STOCK_UPDATE_ERROR", "更新库存失败: " + e.getMessage()));
        }
    }
}