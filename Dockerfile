# 使用官方提供的 OpenJDK 17 镜像作为基础镜像
FROM adoptopenjdk:17-jdk-hotspot

# 将当前目录下的所有文件复制到镜像的 /app 目录中
COPY src/main/Dockerfile /app

# 设置工作目录
WORKDIR /app

FROM maven:3.6.3-jdk-8 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app

# 构建项目（根据具体情况选择适当的构建工具和命令）
RUN mvn -f /usr/src/app/pom.xml clean package -DskipTests=true

ENTRYPOINT [ "sh", "-c", "java -jar /app.jar" ]