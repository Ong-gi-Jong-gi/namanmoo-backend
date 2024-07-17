# Base image
FROM ubuntu:latest

# Install OpenJDK 17 and FFmpeg
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 인자 설정 - JAR_FILE
ARG JAR_FILE=build/libs/*.jar

# JAR 파일을 이미지에 복사
COPY ${JAR_FILE} mooluck-spring.jar

# 환경 변수 설정
ENV TZ=Asia/Seoul
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV FFPROBE_PATH=/usr/bin/ffprobe

# 포트 노출
EXPOSE 8080

# 기본 프로파일 실행 명령어
ENTRYPOINT ["java", "-jar", "/mooluck-spring.jar"]