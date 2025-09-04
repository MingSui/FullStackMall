@echo off
REM FullStackMall 后端独立启动脚本

echo ========================================
echo FullStackMall 后端独立启动脚本
echo ========================================

echo [1/2] 切换到后端目录...
cd /d "d:\Kunlun\FullStackMall\backend"

echo [2/2] 启动后端服务（无数据库模式）...
echo 启动地址: http://localhost:8080
echo API文档: http://localhost:8080/api/swagger-ui.html
echo.

java -jar -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration target\fullstackmall-backend-1.0.0.jar

pause