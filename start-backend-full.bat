@echo off
REM FullStackMall 后端+MySQL 启动脚本

echo ========================================
echo FullStackMall 后端+MySQL 启动脚本
echo ========================================

echo [1/5] 检查Docker环境...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)
echo ✅ Docker环境正常

echo [2/5] 启动MySQL数据库容器...
docker run -d --name fullstackmall-mysql ^
    -e MYSQL_ROOT_PASSWORD=rootpassword ^
    -e MYSQL_DATABASE=fullstackmall ^
    -p 3306:3306 ^
    mysql:8.0.35

if %errorlevel% neq 0 (
    echo 📝 MySQL容器可能已存在，尝试启动现有容器...
    docker start fullstackmall-mysql
)

echo [3/5] 等待MySQL初始化完成...
timeout /t 15 /nobreak >nul

echo [4/5] 切换到后端目录并编译...
cd /d "%~dp0backend"

if not exist "target\fullstackmall-backend-1.0.0.jar" (
    echo 正在编译项目...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo ❌ 编译失败，请检查错误信息
        pause
        exit /b 1
    )
)

echo [5/5] 启动后端服务...
echo.
echo 🚀 启动地址: http://localhost:8080
echo 📚 Knife4j文档: http://localhost:8080/api/doc.html
echo 📊 健康检查: http://localhost:8080/api/health
echo 💾 MySQL: localhost:3306
echo.
echo 🔄 正在启动服务...

start "FullStackMall Backend" java -jar target\fullstackmall-backend-1.0.0.jar

echo ✅ 系统启动完成！
echo.
echo 💡 使用说明：
echo   - 后端服务在新窗口运行
echo   - MySQL数据库运行在Docker容器中
echo   - 关闭后端窗口可停止后端服务
echo   - 停止MySQL: docker stop fullstackmall-mysql
echo   - 删除MySQL: docker rm fullstackmall-mysql
echo.
echo 🌐 点击访问Knife4j文档...
timeout /t 3 /nobreak >nul
start http://localhost:8080/api/doc.html

pause