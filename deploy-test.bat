@echo off
:: FullStackMall 部署测试脚本 (Windows 版本)
:: 用于验证 Docker 容器化部署的完整性和功能性

setlocal enabledelayedexpansion

echo =========================================
echo FullStackMall 部署测试开始
echo =========================================

:: 检查 Docker 和 Docker Compose 是否安装
echo 检查前置条件...

docker --version >nul 2>&1
if errorlevel 1 (
    echo 错误: Docker 未安装或未在 PATH 中
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo 错误: Docker Compose 未安装或未在 PATH 中
    exit /b 1
)

echo ✓ Docker 和 Docker Compose 已安装

:: 清理之前的部署
echo 清理之前的部署...
docker-compose down --volumes --remove-orphans >nul 2>&1
docker system prune -f >nul 2>&1
echo ✓ 清理完成

:: 构建和启动服务
echo 构建和启动服务...

echo 构建镜像...
docker-compose build --no-cache
if errorlevel 1 (
    echo 错误: 镜像构建失败
    exit /b 1
)

echo 启动服务...
docker-compose up -d
if errorlevel 1 (
    echo 错误: 服务启动失败
    exit /b 1
)
echo ✓ 服务启动完成

:: 等待服务就绪
echo 等待服务就绪...

:: 等待 MySQL 就绪
echo 等待 MySQL 数据库启动...
set /a timeout=60
:wait_mysql
docker-compose exec -T mysql mysqladmin ping -h localhost --silent >nul 2>&1
if errorlevel 0 goto mysql_ready
set /a timeout=timeout-1
if !timeout! equ 0 (
    echo 错误: MySQL 启动超时
    docker-compose logs mysql
    exit /b 1
)
timeout /t 1 /nobreak >nul
goto wait_mysql

:mysql_ready
echo ✓ MySQL 已就绪

:: 等待后端服务就绪
echo 等待后端服务启动...
set /a timeout=120
:wait_backend
curl -f http://localhost:8080/api/actuator/health >nul 2>&1
if errorlevel 0 goto backend_ready
set /a timeout=timeout-1
if !timeout! equ 0 (
    echo 错误: 后端服务启动超时
    docker-compose logs backend
    exit /b 1
)
timeout /t 1 /nobreak >nul
goto wait_backend

:backend_ready
echo ✓ 后端服务已就绪

:: 等待前端服务就绪
echo 等待前端服务启动...
set /a timeout=60
:wait_frontend
curl -f http://localhost/health >nul 2>&1
if errorlevel 0 goto frontend_ready
set /a timeout=timeout-1
if !timeout! equ 0 (
    echo 错误: 前端服务启动超时
    docker-compose logs frontend
    exit /b 1
)
timeout /t 1 /nobreak >nul
goto wait_frontend

:frontend_ready
echo ✓ 前端服务已就绪

:: 健康检查
echo 执行健康检查...

:: 检查容器状态
echo 检查容器状态...
docker-compose ps
echo ✓ 容器状态检查完成

:: 检查前端访问
echo 检查前端访问...
curl -f http://localhost/ >nul 2>&1
if errorlevel 1 (
    echo 错误: 前端访问失败
    exit /b 1
)
echo ✓ 前端访问正常

:: 检查后端 API
echo 检查后端 API...
curl -s http://localhost:8080/api/actuator/health >nul 2>&1
if errorlevel 1 (
    echo 错误: 后端 API 访问失败
    exit /b 1
)
echo ✓ 后端 API 正常

:: 简单的 API 功能测试
echo 执行 API 功能测试...

:: 测试商品列表获取
echo 测试商品列表获取...
curl -s http://localhost:8080/api/products?page=0^&size=10 >nul 2>&1
if errorlevel 1 (
    echo 错误: 商品列表获取失败
    exit /b 1
)
echo ✓ 商品列表获取成功

:: 检查资源使用情况
echo 检查资源使用情况...
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"

:: 检查日志
echo 检查应用日志...
echo 最近的后端日志:
docker-compose logs --tail=20 backend

echo =========================================
echo ✓ 部署测试完成！
echo ✓ 前端地址: http://localhost
echo ✓ 后端 API: http://localhost:8080/api
echo ✓ Swagger UI: http://localhost:8080/api/swagger-ui.html
echo ✓ 使用 'docker-compose down' 来停止服务
echo =========================================

pause