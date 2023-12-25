FROM openjdk:19
COPY target/chatgpt-plus-1.0.1-SNAPSHOT.jar /app.jar
EXPOSE 15600
CMD ["java", "-jar", "/app.jar"]