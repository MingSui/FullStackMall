@echo off
REM FullStackMall 后端 Docker 网络测试脚本

echo ========================================
echo FullStackMall 后端 Docker 网络连接测试
echo ========================================

echo [1/3] 检查 MySQL 容器状态...
docker-compose ps mysql

echo [2/3] 检查网络连接...
docker network ls | findstr fullstackmall

echo [3/3] 在 Docker 网络中运行后端应用...
echo 注意：这将在 Docker 网络环境中运行后端
echo.

REM 使用 Docker 运行 Java 应用，连接到相同网络
docker run --rm -it ^
  --network fullstackmall_fullstackmall-network ^
  -p 8080:8080 ^
  -v "%cd%\backend\target:/app" ^
  -w /app ^
  -e SPRING_PROFILES_ACTIVE=docker ^
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/fullstackmall?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" ^
  -e SPRING_DATASOURCE_USERNAME=app_user ^
  -e SPRING_DATASOURCE_PASSWORD=app_password ^
  -e JWT_SECRET=fullstackmall-docker-secret-key-for-production-environment ^
  openjdk:17-jre-slim java -jar fullstackmall-backend-1.0.0.jar

pause