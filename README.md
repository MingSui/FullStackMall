# FullStackMall - 全栈商城项目

## 项目概述
FullStackMall是一个现代化的电子商务平台，采用前后端分离架构。

## 技术栈
- **前端**: React 18 + TypeScript + Vite + Tailwind CSS
- **后端**: Java 17 + Spring Boot 3.x + Spring Security + MySQL
- **部署**: Docker + Docker Compose

## 项目结构
```
FullStackMall/
├── frontend/          # React前端项目
├── backend/           # Spring Boot后端项目
├── docker-compose.yml # Docker编排配置
└── README.md          # 项目说明
```

## 快速开始

### 前端开发
```bash
cd frontend
npm install
npm run dev
```

### 后端开发
```bash
cd backend
mvn spring-boot:run
```

### Docker部署
```bash
docker-compose up -d
```

## MVP功能清单
- [x] 项目结构初始化
- [ ] 用户注册/登录
- [ ] 商品浏览与搜索
- [ ] 购物车管理
- [ ] 订单处理
- [ ] 基础管理后台

## API文档
后端启动后访问: http://localhost:8080/swagger-ui.html

## 前端访问
开发环境: http://localhost:5173