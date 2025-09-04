@echo off
echo 正在构建后端JAR文件...
cd backend
mvn clean package -DskipTests
echo 构建完成！
pause