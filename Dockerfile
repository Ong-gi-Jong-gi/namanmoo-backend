# jdk17 Image Start
FROM openjdk:17-slim

# Firebase Secret 파일 인자 추가
ARG FIREBASE_JSON_FILENAME=mooluck-fcm-firebase-adminsdk.json
COPY src/main/resources/$FIREBASE_JSON_FILENAME /app/$FIREBASE_JSON_FILENAME

# 인자 설정 - JAR_FILE
ARG JAR_FILE=build/libs/*.jar

# jar File Copy
COPY ${JAR_FILE} mooluck-spring.jar

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y ffmpeg

ENV TZ=Asia/Seoul
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV FFPROBE_PATH=/usr/bin/ffprobe

# 기본 프로파일 실행 명령어
ENTRYPOINT ["java", "-Dfirebase.config.path=/app/mooluck-fcm-firebase-adminsdk.json", "-jar", "mooluck-spring.jar", "-Dspring.profiles.active=docker", "-Duser.timezone=Asia/Seoul"]