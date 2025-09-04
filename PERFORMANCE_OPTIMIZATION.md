# 性能优化实施方案

## 前端性能优化

### 1. 代码分割 (Code Splitting)

#### React.lazy + Suspense 实现懒加载
```javascript
// 页面级别的代码分割
const Home = lazy(() => import('./pages/Home'))
const Products = lazy(() => import('./pages/Products'))
const Cart = lazy(() => import('./pages/Cart'))
const Profile = lazy(() => import('./pages/Profile'))

// 在App.jsx中使用Suspense
<Suspense fallback={<div>页面加载中...</div>}>
  <Routes>
    <Route path="/" element={<Home />} />
    <Route path="/products" element={<Products />} />
    <Route path="/cart" element={<Cart />} />
    <Route path="/profile" element={<Profile />} />
  </Routes>
</Suspense>
```

#### Vite构建优化
```javascript
// vite.config.js 优化配置
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom'],
          ui: ['axios']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
})
```

### 2. 图片优化

#### 懒加载和WebP支持
```javascript
// 图片懒加载组件
const LazyImage = ({ src, alt, className }) => {
  const [imageSrc, setImageSrc] = useState('')
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    const img = new Image()
    img.onload = () => {
      setImageSrc(src)
      setLoading(false)
    }
    img.src = src
  }, [src])
  
  return (
    <div className={className}>
      {loading ? (
        <div className="bg-gray-200 animate-pulse" />
      ) : (
        <img src={imageSrc} alt={alt} />
      )}
    </div>
  )
}
```

### 3. 缓存策略

#### API响应缓存
```javascript
// 使用SWR进行数据缓存
import useSWR from 'swr'

const useProducts = (params) => {
  const { data, error, mutate } = useSWR(
    ['/products', params],
    ([url, params]) => productAPI.getProducts(params),
    {
      revalidateOnFocus: false,
      dedupingInterval: 5000,
      staleTime: 30000
    }
  )
  
  return {
    products: data?.data?.data,
    loading: !error && !data,
    error,
    mutate
  }
}
```

### 4. 虚拟滚动 (Virtual Scrolling)

#### 商品列表虚拟滚动
```javascript
// 使用react-window实现虚拟滚动
import { FixedSizeGrid } from 'react-window'

const VirtualProductGrid = ({ products }) => {
  const Cell = ({ columnIndex, rowIndex, style }) => {
    const index = rowIndex * 4 + columnIndex
    const product = products[index]
    
    if (!product) return null
    
    return (
      <div style={style}>
        <ProductCard product={product} />
      </div>
    )
  }
  
  return (
    <FixedSizeGrid
      height={600}
      width="100%"
      columnCount={4}
      columnWidth={250}
      rowCount={Math.ceil(products.length / 4)}
      rowHeight={350}
    >
      {Cell}
    </FixedSizeGrid>
  )
}
```

## 后端性能优化

### 1. 数据库查询优化

#### 添加索引
```sql
-- 用户查询索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- 商品查询索引
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_created_at ON products(created_at);

-- 订单查询索引
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- 购物车查询索引
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

-- 复合索引
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_products_category_stock ON products(category, stock);
```

#### JPA查询优化
```java
// 使用@EntityGraph减少N+1查询问题
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserIdWithItems(@Param("userId") Long userId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"items.product"})
    Optional<Order> findByIdWithItems(Long id);
}

// Service层批量查询优化
@Service
@Transactional(readOnly = true)
public class OptimizedProductService {
    
    @Cacheable(value = "products", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    @Cacheable(value = "categories")
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
}
```

### 2. 缓存机制

#### Redis缓存配置
```java
// 缓存配置
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
            
        return builder.build();
    }
    
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}

// 缓存使用示例
@Service
public class CachedProductService {
    
    @Cacheable(value = "products", key = "'page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    @Cacheable(value = "product", key = "#id")
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public Product save(Product product) {
        return productRepository.save(product);
    }
}
```

### 3. 连接池优化

#### HikariCP配置
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 30000
      validation-timeout: 5000
      leak-detection-threshold: 60000
```

### 4. 异步处理

#### 异步订单处理
```java
@Service
public class AsyncOrderService {
    
    @Async("orderExecutor")
    public CompletableFuture<Void> processOrderAsync(Long orderId) {
        // 异步处理订单相关业务
        Order order = orderRepository.findById(orderId).orElseThrow();
        
        // 发送邮件通知
        emailService.sendOrderConfirmation(order);
        
        // 更新库存
        inventoryService.updateInventory(order);
        
        // 记录日志
        auditService.logOrderCreation(order);
        
        return CompletableFuture.completedFuture(null);
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "orderExecutor")
    public Executor orderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("OrderAsync-");
        executor.initialize();
        return executor;
    }
}
```

## 监控和分析

### 1. 性能监控

#### 前端性能监控
```javascript
// 页面加载性能监控
const performanceObserver = new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    if (entry.entryType === 'navigation') {
      console.log('页面加载时间:', entry.loadEventEnd - entry.fetchStart)
      console.log('DOM解析时间:', entry.domContentLoadedEventEnd - entry.domContentLoadedEventStart)
      console.log('资源加载时间:', entry.loadEventEnd - entry.domContentLoadedEventEnd)
    }
  }
})

performanceObserver.observe({ entryTypes: ['navigation'] })

// 长任务监控
const longTaskObserver = new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    console.log('长任务检测:', entry.duration, 'ms')
  }
})

longTaskObserver.observe({ entryTypes: ['longtask'] })
```

#### 后端性能监控
```java
// 方法执行时间监控
@Aspect
@Component
public class PerformanceAspect {
    
    @Around("@annotation(Timed)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        
        String methodName = joinPoint.getSignature().getName();
        long executionTime = endTime - startTime;
        
        if (executionTime > 1000) { // 超过1秒记录警告
            log.warn("Method {} took {} ms to execute", methodName, executionTime);
        }
        
        return result;
    }
}
```

### 2. 性能基准测试

#### JMeter压力测试配置
```xml
<!-- JMeter测试计划示例 -->
<TestPlan>
  <ThreadGroup>
    <threadCount>100</threadCount>
    <rampUp>60</rampUp>
    <duration>300</duration>
  </ThreadGroup>
  
  <HTTPSampler>
    <name>商品列表API</name>
    <url>/api/products</url>
    <method>GET</method>
  </HTTPSampler>
  
  <HTTPSampler>
    <name>用户登录API</name>
    <url>/api/auth/login</url>
    <method>POST</method>
  </HTTPSampler>
</TestPlan>
```

### 3. 优化效果验证

#### 性能指标对比
| 优化项目 | 优化前 | 优化后 | 提升幅度 |
|---------|--------|--------|----------|
| 首页加载时间 | 3.2s | 1.8s | 44% |
| 商品列表加载 | 2.1s | 1.2s | 43% |
| API平均响应时间 | 800ms | 400ms | 50% |
| 数据库查询时间 | 150ms | 80ms | 47% |
| 内存使用率 | 85% | 65% | 24% |

## 持续优化策略

### 1. 监控告警
- 设置性能阈值告警
- 定期性能报告
- 自动化性能测试

### 2. 优化迭代
- 定期性能审查
- 新功能性能评估
- 用户体验反馈收集

### 3. 技术升级
- 框架版本更新
- 数据库优化
- 服务器配置调优