@echo off
REM FullStackMall 后端启动脚本（使用原始配置）

echo ========================================
echo FullStackMall 后端启动（使用原始配置）
echo ========================================

echo [1/3] 切换到后端目录...
cd /d "%~dp0backend"

echo [2/3] 检查是否需要编译...
if not exist "target\fullstackmall-backend-1.0.0.jar" (
    echo 正在编译项目...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo ❌ 编译失败，请检查错误信息
        pause
        exit /b 1
    )
)

echo [3/3] 启动后端服务...
echo.
echo 🚀 启动地址: http://localhost:8080
echo 📚 Knife4j文档: http://localhost:8080/api/doc.html
echo 📊 健康检查: http://localhost:8080/api/health
echo.
echo ⚠️  注意: 请确保MySQL数据库已启动并可连接
echo    数据库地址: 127.0.0.1:3306
echo    数据库名: fullstackmall
echo    用户名: root
echo    密码: rootpassword
echo.
echo 🔄 正在启动服务...

start "FullStackMall Backend" java -jar target\fullstackmall-backend-1.0.0.jar

echo ✅ 后端服务已在新窗口启动
echo.
echo 💡 提示：
echo   - 新窗口中会显示启动日志
echo   - 关闭新窗口即可停止服务
echo   - 如需查看Knife4j文档，请访问: http://localhost:8080/api/doc.html
echo.
pause