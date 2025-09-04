package com.fullstackmall.service;

import com.fullstackmall.entity.Product;
import com.fullstackmall.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private List<Product> testProducts;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(10);
        testProduct.setCategory("Electronics");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Test Product 2");
        product2.setDescription("Test Description 2");
        product2.setPrice(new BigDecimal("199.99"));
        product2.setStock(5);
        product2.setCategory("Electronics");

        testProducts = Arrays.asList(testProduct, product2);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findAll_Success() {
        // Given
        Page<Product> productPage = new PageImpl<>(testProducts, pageable, testProducts.size());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        // When
        Page<Product> result = productService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(testProduct.getName(), result.getContent().get(0).getName());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void findById_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProduct, result.get());
        verify(productRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.findById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(productRepository).findById(1L);
    }

    @Test
    void searchProducts_Success() {
        // Given
        Page<Product> productPage = new PageImpl<>(testProducts, pageable, testProducts.size());
        when(productRepository.findProducts(anyString(), anyString(), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<Product> result = productService.searchProducts("test", "Electronics", pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(productRepository).findProducts("test", "Electronics", pageable);
    }

    @Test
    void findByCategory_Success() {
        // Given
        Page<Product> productPage = new PageImpl<>(testProducts, pageable, testProducts.size());
        when(productRepository.findByCategory(anyString(), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<Product> result = productService.findByCategory("Electronics", pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(productRepository).findByCategory("Electronics", pageable);
    }

    @Test
    void findByNameContaining_Success() {
        // Given
        Page<Product> productPage = new PageImpl<>(testProducts, pageable, testProducts.size());
        when(productRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<Product> result = productService.findByNameContaining("test", pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(productRepository).findByNameContainingIgnoreCase("test", pageable);
    }

    @Test
    void findInStock_Success() {
        // Given
        Page<Product> productPage = new PageImpl<>(testProducts, pageable, testProducts.size());
        when(productRepository.findByStockGreaterThan(anyInt(), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<Product> result = productService.findInStock(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(productRepository).findByStockGreaterThan(0, pageable);
    }

    @Test
    void getAllCategories_Success() {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Books", "Clothing");
        when(productRepository.findAllCategories()).thenReturn(categories);

        // When
        List<String> result = productService.getAllCategories();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Electronics"));
        verify(productRepository).findAllCategories();
    }

    @Test
    void save_Success() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.save(testProduct);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository).save(testProduct);
    }

    @Test
    void update_Success() {
        // Given
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(new BigDecimal("149.99"));
        updatedProduct.setStock(15);
        updatedProduct.setCategory("Updated Category");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.update(1L, updatedProduct);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
    }

    @Test
    void update_ProductNotFound_ThrowsException() {
        // Given
        Product updatedProduct = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.update(1L, updatedProduct));
        
        assertEquals("商品不存在: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void delete_Success() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);

        // When
        productService.delete(1L);

        // Then
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_ProductNotFound_ThrowsException() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.delete(1L));
        
        assertEquals("商品不存在: 1", exception.getMessage());
        verify(productRepository).existsById(1L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void decreaseStock_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.decreaseStock(1L, 5);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        assertEquals(5, testProduct.getStock()); // 10 - 5 = 5
    }

    @Test
    void decreaseStock_InsufficientStock_ThrowsException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.decreaseStock(1L, 15)); // More than available stock
        
        assertEquals("库存不足", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void decreaseStock_ProductNotFound_ThrowsException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.decreaseStock(1L, 5));
        
        assertEquals("商品不存在: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void increaseStock_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.increaseStock(1L, 5);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        assertEquals(15, testProduct.getStock()); // 10 + 5 = 15
    }

    @Test
    void increaseStock_ProductNotFound_ThrowsException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.increaseStock(1L, 5));
        
        assertEquals("商品不存在: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
}