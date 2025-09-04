# 使用多阶段构建
# 第一阶段：构建阶段
FROM maven:3.8.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制pom.xml和maven配置
COPY pom.xml .
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装wget用于健康检查
RUN apk add --no-cache wget

# 创建非root用户
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring

# 复制构建好的jar文件
COPY --from=builder /app/target/*.jar app.jar

# 更改文件所有权
RUN chown spring:spring app.jar

# 切换到非root用户
USER spring:spring

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]