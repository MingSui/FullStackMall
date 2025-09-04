# FullStackMall 全栈商城系统

一个现代化的前后端分离电商平台，采用React + Spring Boot + MySQL技术栈，通过Docker容器化部署。

## 🏗️ 技术架构

- **前端**: React 18 + TypeScript + Vite + Tailwind CSS
- **后端**: Java 17 + Spring Boot 3.2 + Spring Security + JWT
- **数据库**: MySQL 8.0
- **部署**: Docker + Docker Compose

## 🚀 快速启动

### 方式一：Docker容器化部署（推荐）

1. **确保Docker环境已安装并运行**
   ```bash
   # 验证Docker是否正常运行
   docker --version
   docker-compose --version
   ```

2. **一键启动整个系统**
   ```bash
   # 构建并启动所有服务
   docker-compose up --build -d
   ```

3. **访问系统**
   - 前端网站: http://localhost
   - 后端API文档: http://localhost:8080/api/swagger-ui.html
   - 数据库: localhost:3306

4. **查看服务状态**
   ```bash
   # 查看所有服务状态
   docker-compose ps
   
   # 查看服务日志
   docker-compose logs -f [service_name]
   ```

5. **停止系统**
   ```bash
   docker-compose down
   ```

### 方式二：本地开发模式

#### 后端启动
```bash
cd backend
mvn spring-boot:run
```

#### 前端启动（需要先安装依赖）
```bash
cd frontend
npm install
npm run dev
```

## 📁 项目结构

```
FullStackMall/
├── frontend/                    # React前端项目
│   ├── src/
│   │   ├── components/         # 组件
│   │   ├── pages/             # 页面
│   │   ├── services/          # API服务
│   │   └── ...
│   ├── Dockerfile
│   └── package.json
├── backend/                     # Spring Boot后端项目
│   ├── src/main/
│   │   ├── java/com/fullstackmall/
│   │   │   ├── controller/    # API控制器
│   │   │   ├── dto/          # 数据传输对象
│   │   │   ├── entity/       # JPA实体类
│   │   │   ├── repository/   # 数据访问层
│   │   │   └── service/      # 业务逻辑层
│   │   └── resources/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml           # 容器编排配置
├── .gitignore                  # Git忽略文件
└── README.md                   # 项目说明文档
```

## 🌟 功能特性

### 已实现功能
- ✅ 用户注册/登录界面
- ✅ 商品浏览和展示
- ✅ 购物车管理
- ✅ 个人中心
- ✅ 响应式设计
- ✅ JWT身份认证
- ✅ API文档（Swagger）
- ✅ Docker容器化部署

### 待开发功能
- 🔄 用户认证API集成
- 🔄 商品管理API
- 🔄 订单处理系统
- 🔄 支付集成
- 🔄 管理后台

## 🔧 开发配置

### 环境要求
- Node.js 18+
- Java 17
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0（如果不使用Docker）

### 环境变量配置
系统支持通过环境变量配置数据库连接：

```yaml
# 数据库配置
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/fullstackmall
SPRING_DATASOURCE_USERNAME: mall_user
SPRING_DATASOURCE_PASSWORD: mall_password
```

## 📝 API文档

启动后端服务后，可以通过以下地址访问API文档：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/api-docs

## 🛠️ 开发指南

### 添加新功能
1. 后端：在对应的package中添加controller、service、repository
2. 前端：在src目录下添加相应的组件和页面
3. 更新API服务文件以集成新的后端接口

### 数据库管理
- 系统使用JPA自动建表，首次启动会自动创建所需表结构
- 开发环境下，可以设置`spring.jpa.hibernate.ddl-auto=create-drop`进行表结构重建

### 部署说明
- 生产环境建议使用环境变量配置敏感信息
- 可以通过修改docker-compose.yml中的端口映射来自定义访问端口
- 建议配置HTTPS和数据备份策略

## 🚨 注意事项

1. **首次启动**可能需要几分钟时间，因为需要下载依赖和构建镜像
2. **数据持久化**：MySQL数据通过Docker卷持久化，删除容器不会丢失数据
3. **端口占用**：确保80、8080、3306端口未被其他服务占用
4. **内存要求**：建议系统可用内存不少于4GB

## 🔍 故障排除

### 常见问题

1. **Docker启动失败**
   ```bash
   # 检查Docker服务状态
   docker info
   ```

2. **端口冲突**
   ```bash
   # 修改docker-compose.yml中的端口映射
   ports:
     - "8081:8080"  # 改用8081端口
   ```

3. **数据库连接失败**
   ```bash
   # 查看数据库日志
   docker-compose logs mysql
   ```

4. **前端构建失败**
   ```bash
   # 清理缓存重新构建
   docker-compose build --no-cache frontend
   ```

## 📞 技术支持

如遇到问题，请检查：
1. Docker和Docker Compose是否正确安装
2. 相关端口是否被占用
3. 系统资源是否充足

---

**Powered by React + Spring Boot + Docker**