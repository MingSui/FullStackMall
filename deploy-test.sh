#!/bin/bash

# FullStackMall 部署测试脚本
# 用于验证 Docker 容器化部署的完整性和功能性

set -e

echo "========================================="
echo "FullStackMall 部署测试开始"
echo "========================================="

# 检查 Docker 和 Docker Compose 是否安装
check_prerequisites() {
    echo "检查前置条件..."
    
    if ! command -v docker &> /dev/null; then
        echo "错误: Docker 未安装或未在 PATH 中"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        echo "错误: Docker Compose 未安装或未在 PATH 中"
        exit 1
    fi
    
    echo "✓ Docker 和 Docker Compose 已安装"
}

# 清理之前的部署
cleanup_previous_deployment() {
    echo "清理之前的部署..."
    
    docker-compose down --volumes --remove-orphans 2>/dev/null || true
    docker system prune -f 2>/dev/null || true
    
    echo "✓ 清理完成"
}

# 构建和启动服务
build_and_start_services() {
    echo "构建和启动服务..."
    
    echo "构建镜像..."
    docker-compose build --no-cache
    
    echo "启动服务..."
    docker-compose up -d
    
    echo "✓ 服务启动完成"
}

# 等待服务就绪
wait_for_services() {
    echo "等待服务就绪..."
    
    # 等待 MySQL 就绪
    echo "等待 MySQL 数据库启动..."
    timeout=60
    while ! docker-compose exec -T mysql mysqladmin ping -h localhost --silent; do
        timeout=$((timeout - 1))
        if [ $timeout -eq 0 ]; then
            echo "错误: MySQL 启动超时"
            docker-compose logs mysql
            exit 1
        fi
        sleep 1
    done
    echo "✓ MySQL 已就绪"
    
    # 等待后端服务就绪
    echo "等待后端服务启动..."
    timeout=120
    while ! curl -f http://localhost:8080/api/actuator/health &>/dev/null; do
        timeout=$((timeout - 1))
        if [ $timeout -eq 0 ]; then
            echo "错误: 后端服务启动超时"
            docker-compose logs backend
            exit 1
        fi
        sleep 1
    done
    echo "✓ 后端服务已就绪"
    
    # 等待前端服务就绪
    echo "等待前端服务启动..."
    timeout=60
    while ! curl -f http://localhost/health &>/dev/null; do
        timeout=$((timeout - 1))
        if [ $timeout -eq 0 ]; then
            echo "错误: 前端服务启动超时"
            docker-compose logs frontend
            exit 1
        fi
        sleep 1
    done
    echo "✓ 前端服务已就绪"
}

# 健康检查
health_check() {
    echo "执行健康检查..."
    
    # 检查容器状态
    echo "检查容器状态..."
    if ! docker-compose ps | grep -q "Up"; then
        echo "错误: 某些容器未正常运行"
        docker-compose ps
        exit 1
    fi
    echo "✓ 所有容器正常运行"
    
    # 检查数据库连接
    echo "检查数据库连接..."
    if ! docker-compose exec -T mysql mysql -u app_user -papp_password -e "SELECT 1;" fullstackmall &>/dev/null; then
        echo "错误: 数据库连接失败"
        exit 1
    fi
    echo "✓ 数据库连接正常"
    
    # 检查后端 API
    echo "检查后端 API..."
    backend_health=$(curl -s http://localhost:8080/api/actuator/health | jq -r '.status' 2>/dev/null || echo "ERROR")
    if [ "$backend_health" != "UP" ]; then
        echo "错误: 后端健康检查失败"
        exit 1
    fi
    echo "✓ 后端 API 健康"
    
    # 检查前端访问
    echo "检查前端访问..."
    if ! curl -f http://localhost/ &>/dev/null; then
        echo "错误: 前端访问失败"
        exit 1
    fi
    echo "✓ 前端访问正常"
}

# API 功能测试
api_functional_test() {
    echo "执行 API 功能测试..."
    
    BASE_URL="http://localhost:8080/api"
    
    # 测试用户注册
    echo "测试用户注册..."
    register_response=$(curl -s -X POST "$BASE_URL/auth/register" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "testuser",
            "email": "test@example.com",
            "password": "password123"
        }')
    
    if ! echo "$register_response" | jq -e '.success == true' &>/dev/null; then
        echo "错误: 用户注册失败"
        echo "响应: $register_response"
        exit 1
    fi
    echo "✓ 用户注册成功"
    
    # 测试用户登录
    echo "测试用户登录..."
    login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "testuser",
            "password": "password123"
        }')
    
    if ! echo "$login_response" | jq -e '.success == true' &>/dev/null; then
        echo "错误: 用户登录失败"
        echo "响应: $login_response"
        exit 1
    fi
    
    # 提取 JWT Token
    token=$(echo "$login_response" | jq -r '.data.token')
    if [ "$token" = "null" ] || [ -z "$token" ]; then
        echo "错误: 无法获取 JWT Token"
        exit 1
    fi
    echo "✓ 用户登录成功，Token 获取成功"
    
    # 测试商品列表获取
    echo "测试商品列表获取..."
    products_response=$(curl -s "$BASE_URL/products?page=0&size=10")
    
    if ! echo "$products_response" | jq -e '.success == true' &>/dev/null; then
        echo "错误: 商品列表获取失败"
        echo "响应: $products_response"
        exit 1
    fi
    echo "✓ 商品列表获取成功"
    
    # 测试需要认证的接口（购物车）
    echo "测试购物车接口..."
    cart_response=$(curl -s "$BASE_URL/cart" \
        -H "Authorization: Bearer $token")
    
    if ! echo "$cart_response" | jq -e '.success == true' &>/dev/null; then
        echo "错误: 购物车接口测试失败"
        echo "响应: $cart_response"
        exit 1
    fi
    echo "✓ 购物车接口测试成功"
}

# 性能测试
performance_test() {
    echo "执行基本性能测试..."
    
    # 测试前端首页响应时间
    echo "测试前端首页响应时间..."
    frontend_time=$(curl -o /dev/null -s -w '%{time_total}' http://localhost/)
    echo "前端首页响应时间: ${frontend_time}s"
    
    # 测试后端 API 响应时间
    echo "测试后端 API 响应时间..."
    api_time=$(curl -o /dev/null -s -w '%{time_total}' http://localhost:8080/api/products)
    echo "后端 API 响应时间: ${api_time}s"
    
    echo "✓ 性能测试完成"
}

# 资源使用情况检查
resource_usage_check() {
    echo "检查资源使用情况..."
    
    echo "容器资源使用情况:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
    
    echo "✓ 资源使用检查完成"
}

# 日志检查
log_check() {
    echo "检查应用日志..."
    
    echo "检查后端日志是否有错误..."
    if docker-compose logs backend | grep -i "error\|exception" | grep -v "org.springframework.boot.autoconfigure.logging"; then
        echo "警告: 发现后端错误日志"
    else
        echo "✓ 后端日志正常"
    fi
    
    echo "检查前端日志是否有错误..."
    if docker-compose logs frontend | grep -i "error"; then
        echo "警告: 发现前端错误日志"
    else
        echo "✓ 前端日志正常"
    fi
}

# 主测试流程
main() {
    check_prerequisites
    cleanup_previous_deployment
    build_and_start_services
    wait_for_services
    health_check
    api_functional_test
    performance_test
    resource_usage_check
    log_check
    
    echo "========================================="
    echo "✓ 部署测试全部通过！"
    echo "✓ 前端地址: http://localhost"
    echo "✓ 后端 API: http://localhost:8080/api"
    echo "✓ Swagger UI: http://localhost:8080/api/swagger-ui.html"
    echo "========================================="
}

# 异常处理
trap 'echo "测试过程中发生错误，正在清理..."; docker-compose logs; docker-compose down' ERR

# 执行主流程
main