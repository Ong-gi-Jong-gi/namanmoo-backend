# Docker file

# jdk17 Image Start
FROM openjdk:17

# 인자 설정 - JAR_FILE
ARG JAR_FILE=build/libs/*.jar

# jar File Copy
COPY ${JAR_FILE} mooluckmooluck.jar

# 인자 설정 부분과 jar 파일 복제 부분 합쳐서 진행해도 무방
#COPY build/libs/*.jar mooluckmooluck.jar

# 기본 프로파일 실행 명령어
ENTRYPOINT ["java", "-jar", "mooluckmooluck.jar"]

# docker 프로파일로 실행
#ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]



#FROM openjdk:17 AS builder
#WORKDIR /app
#
#COPY ./gradlew .
#COPY ./gradle .
#COPY ./build.gradle .
#COPY ./settings.gradle .
#COPY ./src .
## gradlew 실행권한 부여
#RUN chmod +x ./gradlew
#RUN microdnf install findutils
## jar 파일 생성
#RUN ./gradlew bootJar
#
#FROM openjdk:17
#WORKDIR /app
#COPY --from=builder /app/build/libs/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]