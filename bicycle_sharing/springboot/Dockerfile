# --- Stage 1: Build ---
# 使用一个包含 Maven 和 JDK 的镜像来编译和打包您的 Java 应用
FROM maven:3.9-eclipse-temurin-21-alpine AS build

# 设置工作目录
WORKDIR /app

# 仅复制 pom.xml，以便利用 Docker 的缓存机制。
# 只有当 pom.xml 变化时，才会重新下载依赖。
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制所有源代码
COPY src ./src

# 运行 Maven 打包命令
RUN mvn package -DskipTests

# --- Stage 2: Run ---
# 使用一个非常轻量的、仅包含 Java 运行环境的镜像来运行应用
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 从上一个“构建”阶段，把打包好的 jar 文件复制过来
COPY --from=build /app/target/springboot-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用程序使用的端口（根据您的 application.yml，默认为 8080）
EXPOSE 8080

# 容器启动时执行的命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]