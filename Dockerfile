# jdk17 Image Start
FROM openjdk:17-slim

# 인자 설정 - JAR_FILE 및 FIREBASE_CONFIG_FILE
ARG JAR_FILE=build/libs/*.jar
ARG FIREBASE_CONFIG_FILE=src/main/resources/mooluck-fcm-firebase-adminsdk.json

# jar File Copy
COPY ${JAR_FILE} mooluck-spring.jar

# Firebase 설정 파일을 컨테이너의 특정 위치로 복사
COPY ${FIREBASE_CONFIG_FILE} /app/mooluck-fcm-firebase-adminsdk.json

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y ffmpeg

ENV TZ=Asia/Seoul
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV FFPROBE_PATH=/usr/bin/ffprobe

# 기본 프로파일 실행 명령어
ENTRYPOINT ["java", "-Dfirebase.config.path=/app/mooluck-fcm-firebase-adminsdk.json", "-Dspring.profiles.active=docker", "-Duser.timezone=Asia/Seoul", "-jar", "mooluck-spring.jar"]
