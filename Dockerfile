# jdk17 이미지 시작
FROM openjdk:17

# 인자 설정 - JAR_FILE
ARG JAR_FILE=build/libs/*.jar

# JAR 파일 복사
COPY ${JAR_FILE} mooluck-spring.jar

# 필요한 패키지 설치 (wget 대신 curl 사용)
RUN apt-get update && apt-get install -y curl && \
    curl -L -o /usr/bin/ffmpeg https://ffmpeg.org/releases/ffmpeg-4.4.tar.gz && \
    tar -xvf /usr/bin/ffmpeg -C /usr/bin/ && \
    chmod +x /usr/bin/ffmpeg && \
    ln -s /usr/bin/ffmpeg /usr/bin/ffprobe

ENV TZ=Asia/Seoul
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV FFPROBE_PATH=/usr/bin/ffprobe

# 기본 프로파일 실행 명령어
ENTRYPOINT ["java", "-jar", "mooluck-spring.jar", "-Dspring.profiles.active=docker", "-Duser.timezone=Asia/Seoul"]
