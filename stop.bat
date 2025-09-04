@echo off
REM FullStackMall 停止脚本

echo ========================================
echo FullStackMall 全栈商城系统停止脚本
echo ========================================

echo 正在停止所有服务...
docker-compose down

if %errorlevel% neq 0 (
    echo ❌ 停止失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo ✅ 系统已成功停止
echo.
echo 💡 提示:
echo - 数据已保存，下次启动时数据不会丢失
echo - 如需完全清理（包括数据），请运行: docker-compose down -v
echo.
pause