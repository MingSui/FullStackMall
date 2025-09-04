@echo off
REM FullStackMall 启动脚本

echo ========================================
echo FullStackMall 全栈商城系统启动脚本
echo ========================================

REM 检查Docker是否运行
echo [1/4] 检查Docker环境...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: Docker未运行，请先启动Docker Desktop
    echo.
    echo 请按照以下步骤操作:
    echo 1. 启动Docker Desktop应用程序
    echo 2. 等待Docker完全启动
    echo 3. 重新运行此脚本
    pause
    exit /b 1
)
echo ✅ Docker环境正常

REM 检查端口是否被占用
echo [2/4] 检查端口占用情况...
netstat -an | findstr ":80 " >nul
if %errorlevel% equ 0 (
    echo ⚠️  警告: 端口80已被占用，可能影响前端访问
)
netstat -an | findstr ":8080 " >nul
if %errorlevel% equ 0 (
    echo ⚠️  警告: 端口8080已被占用，可能影响后端访问
)
netstat -an | findstr ":3306 " >nul
if %errorlevel% equ 0 (
    echo ⚠️  警告: 端口3306已被占用，可能影响数据库访问
)

REM 构建并启动服务
echo [3/4] 构建并启动服务...
echo 这可能需要几分钟时间，请耐心等待...
docker-compose up --build -d

if %errorlevel% neq 0 (
    echo ❌ 启动失败，请检查错误信息
    pause
    exit /b 1
)

REM 等待服务启动
echo [4/4] 等待服务启动完成...
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo 🎉 系统启动成功！
echo ========================================
echo.
echo 📱 前端网站: http://localhost
echo 🔧 后端API: http://localhost:8080/api/swagger-ui.html
echo 💾 数据库: localhost:3306
echo.
echo 📊 查看服务状态: docker-compose ps
echo 📋 查看日志: docker-compose logs -f [service_name]
echo 🛑 停止系统: docker-compose down
echo.
echo 按任意键打开前端网站...
pause >nul
start http://localhost