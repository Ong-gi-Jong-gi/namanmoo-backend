# Docker file

# jdk17 Image Start
FROM openjdk:17

# 인자 설정 - JAR_FILE
ARG JAR_FILE=build/libs/*.jar

# jar File Copy
COPY ${JAR_FILE} mooluckmooluck.jar

# 기본 프로파일 실행 명령어
ENTRYPOINT ["java", "-jar", "mooluckmooluck.jar"]