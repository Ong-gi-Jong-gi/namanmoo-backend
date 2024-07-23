# jdk17 Image Start
FROM openjdk:17-slim

# 인자 설정 - JAR_FILE
ARG JAR_FILE=build/libs/*.jar

# jar File Copy
COPY ${JAR_FILE} mooluck-spring.jar
# Firebase config file copy
COPY src/main/resources/mooluck-fcm-firebase-adminsdk-cjwp5-cd30150b1a.json /app/resources/mooluck-fcm-firebase-adminsdk-cjwp5-cd30150b1a.json


# 필요한 패키지 설치
RUN apt-get update && apt-get install -y ffmpeg

ENV TZ=Asia/Seoul
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV FFPROBE_PATH=/usr/bin/ffprobe
ENV FIREBASE_CONFIG_PATH=/app/resources/mooluck-fcm-firebase-adminsdk-cjwp5-cd30150b1a.json

# 기본 프로파일 실행 명령어
ENTRYPOINT ["java", "-jar", "mooluck-spring.jar", "-Dspring.profiles.active=docker", "-Duser.timezone=Asia/Seoul"]