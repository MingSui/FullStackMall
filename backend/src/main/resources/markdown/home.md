# FullStackMall API 文档

欢迎使用 FullStackMall 商城系统 API 文档！

## 项目概述

FullStackMall 是一个现代化的电子商务平台，基于 Spring Boot 和 React 构建，提供完整的在线购物体验。

## 技术栈

- **后端**: Spring Boot 3.x + Spring Security + JPA + MySQL
- **前端**: React 18 + TypeScript + Vite + Tailwind CSS
- **部署**: Docker + Docker Compose
- **文档**: Knife4j (基于 OpenAPI 3.0)

## 主要功能模块

### 1. 用户认证模块
- 用户注册
- 用户登录
- JWT Token 认证
- 用户权限管理

### 2. 商品管理模块
- 商品浏览
- 商品搜索
- 商品详情
- 分类管理

### 3. 购物车模块
- 添加商品到购物车
- 修改购物车商品数量
- 删除购物车商品
- 购物车列表查看

### 4. 订单管理模块
- 创建订单
- 订单支付
- 订单状态管理
- 订单历史查看

## API 使用说明

### 认证方式

本 API 使用 JWT (JSON Web Token) 进行身份认证。

1. 首先调用登录接口获取 token
2. 在后续请求的 Header 中添加：`Authorization: Bearer {your_token}`

### 响应格式

所有 API 响应都遵循统一的格式：

```json
{
  "success": true,
  "data": {},
  "message": "操作成功",
  "error": null,
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

### 错误码说明

- `200`: 成功
- `201`: 创建成功
- `400`: 请求参数错误
- `401`: 未授权访问
- `403`: 权限不足
- `404`: 资源不存在
- `500`: 服务器内部错误

## 快速开始

1. **注册用户**
   ```bash
   POST /api/auth/register
   ```

2. **用户登录**
   ```bash
   POST /api/auth/login
   ```

3. **获取用户信息**
   ```bash
   GET /api/users/profile
   Header: Authorization: Bearer {token}
   ```

## 联系我们

- **Email**: support@fullstackmall.com
- **GitHub**: https://github.com/fullstackmall/fullstackmall
- **文档**: http://localhost:8080/api/doc.html

---

*本文档由 Knife4j 自动生成，最后更新时间: 2024-01-01*