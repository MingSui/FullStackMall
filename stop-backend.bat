@echo off
REM FullStackMall 系统停止脚本

echo ========================================
echo FullStackMall 系统停止脚本
echo ========================================

echo [1/3] 停止后端Java进程...
for /f "tokens=2" %%i in ('jps -l ^| findstr "FullStackMallApplication"') do (
    echo 正在停止进程 %%i
    taskkill /F /PID %%i
)

echo [2/3] 停止MySQL容器...
docker stop fullstackmall-mysql 2>nul
if %errorlevel% equ 0 (
    echo ✅ MySQL容器已停止
) else (
    echo ℹ️  MySQL容器未运行或不存在
)

echo [3/3] 清理完成
echo.
echo 💡 如需完全删除MySQL数据，请运行：
echo    docker rm fullstackmall-mysql
echo.
echo ✅ 系统已停止
pause