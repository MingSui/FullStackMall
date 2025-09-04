# 多阶段构建 - 使用本地构建的JAR
# 使用本地已有的MySQL镜像，它基于Ubuntu并包含Java环境
# 使用您之前提供的华为云JDK17镜像
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/tomcat:9-jdk17-openjdk-slim

# 设置工作目录
WORKDIR /app

# 复制预构建的JAR文件
COPY target/fullstackmall-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=docker

# 运行应用
CMD ["java", "-jar", "app.jar"]