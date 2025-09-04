# FullStackMall 部署配置指南

## 环境要求

### 开发环境
- **Java**: JDK 17 或更高版本
- **Node.js**: 18.x 或更高版本
- **MySQL**: 8.0 或更高版本
- **Maven**: 3.6 或更高版本
- **Git**: 最新版本

### 生产环境
- **Docker**: 20.10 或更高版本
- **Docker Compose**: 2.0 或更高版本
- **服务器**: 最少 4GB RAM，20GB 存储空间
- **操作系统**: Linux (推荐 Ubuntu 20.04+) 或 Windows 10/11

## 部署方式

### 1. Docker 容器化部署（推荐）

#### 快速启动
```bash
# 克隆项目
git clone <repository-url>
cd FullStackMall

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

#### 自定义配置
可以通过环境变量自定义配置：

```bash
# 创建 .env 文件
cat > .env << EOF
# 数据库配置
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=fullstackmall
MYSQL_USER=your_db_user
MYSQL_PASSWORD=your_db_password

# JWT 配置
JWT_SECRET=your_jwt_secret_key

# 端口配置
FRONTEND_PORT=80
BACKEND_PORT=8080
MYSQL_PORT=3306
EOF

# 使用自定义配置启动
docker-compose --env-file .env up -d
```

### 2. 传统部署方式

#### 后端部署
```bash
cd backend

# 配置数据库连接
cp src/main/resources/application.yml.example src/main/resources/application.yml
# 编辑 application.yml 配置数据库连接信息

# 构建项目
./mvnw clean package -DskipTests

# 运行应用
java -jar target/fullstackmall-backend-0.0.1-SNAPSHOT.jar
```

#### 前端部署
```bash
cd frontend

# 安装依赖
npm install

# 配置 API 地址
cp .env.example .env
# 编辑 .env 文件设置 VITE_API_BASE_URL

# 构建项目
npm run build

# 使用 nginx 或其他 web 服务器提供静态文件服务
```

## 环境配置

### 数据库配置

#### MySQL 配置文件 (my.cnf)
```ini
[mysqld]
# 基础配置
port = 3306
bind-address = 0.0.0.0
max_connections = 200
default-time-zone = '+08:00'

# 字符集配置
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# InnoDB 配置
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 1
innodb_flush_method = O_DIRECT

# 查询缓存
query_cache_type = 1
query_cache_size = 64M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

### Nginx 配置（生产环境）

#### nginx.conf
```nginx
upstream backend {
    server backend:8080;
}

server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
        
        # 缓存配置
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;
}
```

### SSL/HTTPS 配置

#### Let's Encrypt 证书
```bash
# 安装 certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo crontab -e
# 添加以下行
0 12 * * * /usr/bin/certbot renew --quiet
```

## 监控和日志

### 应用监控

#### Prometheus + Grafana
```yaml
# docker-compose.monitoring.yml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

### 日志聚合

#### ELK Stack
```yaml
# docker-compose.logging.yml
version: '3.8'
services:
  elasticsearch:
    image: elasticsearch:7.14.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"
      
  logstash:
    image: logstash:7.14.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
      
  kibana:
    image: kibana:7.14.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
```

## 安全配置

### 防火墙设置
```bash
# Ubuntu UFW
sudo ufw enable
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw deny 3306/tcp   # MySQL (仅内部访问)
sudo ufw deny 8080/tcp   # Backend (仅内部访问)
```

### Docker 安全
```bash
# 创建非 root 用户运行容器
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# 使用多阶段构建减少镜像大小
FROM openjdk:17-jdk-slim AS build
# ... 构建阶段
FROM openjdk:17-jre-slim AS runtime
COPY --from=build /app/target/*.jar app.jar
```

## 备份策略

### 数据库备份
```bash
#!/bin/bash
# backup-db.sh

BACKUP_DIR="/var/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="fullstackmall_backup_$DATE.sql"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
docker-compose exec -T mysql mysqldump -u root -p$MYSQL_ROOT_PASSWORD fullstackmall > $BACKUP_DIR/$BACKUP_FILE

# 压缩备份文件
gzip $BACKUP_DIR/$BACKUP_FILE

# 删除 7 天前的备份
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete

echo "数据库备份完成: $BACKUP_DIR/$BACKUP_FILE.gz"
```

### 定时备份
```bash
# 添加到 crontab
0 2 * * * /path/to/backup-db.sh
```

## 故障排除

### 常见问题

#### 1. 容器启动失败
```bash
# 查看容器日志
docker-compose logs [service-name]

# 检查容器状态
docker-compose ps

# 重启服务
docker-compose restart [service-name]
```

#### 2. 数据库连接失败
```bash
# 检查数据库容器状态
docker-compose exec mysql mysql -u root -p

# 检查网络连接
docker-compose exec backend ping mysql

# 验证环境变量
docker-compose exec backend env | grep SPRING_DATASOURCE
```

#### 3. 前端无法访问后端 API
```bash
# 检查 nginx 配置
docker-compose exec frontend nginx -t

# 检查代理配置
curl -I http://localhost/api/actuator/health

# 检查防火墙设置
sudo ufw status
```

### 性能调优

#### JVM 参数优化
```bash
# 在 docker-compose.yml 中添加 JVM 参数
environment:
  - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

#### 数据库索引优化
```sql
-- 添加常用查询索引
CREATE INDEX idx_products_category_price ON products(category_id, price);
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
```

## 版本升级

### 应用升级流程
```bash
# 1. 备份数据
./backup-db.sh

# 2. 停止服务
docker-compose down

# 3. 拉取新代码
git pull origin main

# 4. 重新构建镜像
docker-compose build --no-cache

# 5. 启动服务
docker-compose up -d

# 6. 验证升级
./deploy-test.sh
```

### 回滚策略
```bash
# 1. 停止当前版本
docker-compose down

# 2. 切换到上一个版本
git checkout <previous-commit>

# 3. 重新部署
docker-compose up -d

# 4. 恢复数据库（如需要）
# 从备份恢复数据库
```