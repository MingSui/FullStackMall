#!/bin/bash
# 完整容器化部署脚本

echo "=========================================="
echo "FullStackMall 完整容器化部署"
echo "=========================================="

echo "[1/3] 停止现有服务..."
docker-compose down

echo "[2/3] 构建并启动所有服务..."
echo "这可能需要几分钟，请等待..."

# 设置国内镜像源环境变量
export DOCKER_BUILDKIT=1

# 启动服务
docker-compose up --build -d

echo "[3/3] 检查服务状态..."
sleep 10
docker-compose ps

echo "=========================================="
echo "🎉 部署完成！"
echo "=========================================="
echo "📱 前端: http://localhost"
echo "🔧 后端API: http://localhost:8080/api"
echo "📊 服务状态: docker-compose ps"
echo "📋 查看日志: docker-compose logs -f [服务名]"
echo "🛑 停止服务: docker-compose down"
echo "=========================================="