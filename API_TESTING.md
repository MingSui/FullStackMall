# API集成测试指南

## 测试环境准备

### 1. 启动后端服务
```bash
cd backend
mvn spring-boot:run
```

### 2. 启动前端服务
```bash
cd frontend
npm install
npm run dev
```

### 3. 数据库准备
确保MySQL数据库正在运行，数据库名称为`fullstackmall`。

## 手动API测试

### 1. 健康检查
```bash
curl http://localhost:8080/api/health
```

### 2. 用户注册
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com", 
    "password": "password123"
  }'
```

### 3. 用户登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 4. 获取商品列表
```bash
curl http://localhost:8080/api/products
```

### 5. 获取购物车（需要认证）
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/cart
```

## 自动化测试

运行API集成测试脚本：
```bash
chmod +x test-api.sh
./test-api.sh
```

## 前端测试步骤

1. 访问 http://localhost:5173
2. 测试用户注册功能
3. 测试用户登录功能
4. 测试商品浏览功能
5. 测试添加到购物车功能
6. 测试购物车管理功能
7. 测试订单创建功能
8. 测试用户个人中心功能

## 常见问题

### CORS问题
如果遇到CORS错误，检查：
1. 后端Controller是否添加了`@CrossOrigin`注解
2. Vite代理配置是否正确

### 认证问题
如果遇到401错误，检查：
1. JWT token是否正确传递
2. token是否已过期
3. 请求头格式是否为`Authorization: Bearer <token>`

### 数据库连接问题
如果遇到数据库连接错误，检查：
1. MySQL服务是否启动
2. 数据库连接配置是否正确
3. 数据库是否已创建

## API文档

启动后端服务后，可以访问：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API文档: http://localhost:8080/api/api-docs