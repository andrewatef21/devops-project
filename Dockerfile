FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY app/Main.java /app/Main.java
EXPOSE 8080
CMD ["java", "/app/Main.java"]
