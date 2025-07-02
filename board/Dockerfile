FROM eclipse-temurin:21-alpine
WORKDIR /app

# 타임존 설정을 위해 필요한 패키지 설치
RUN apk add --no-cache tzdata
# 타임존을 서울로 설정
ENV TZ=Asia/Seoul

RUN ./gradlew :board:clean :board:bootJar

COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]