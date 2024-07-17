# Docker file

# jdk17 Image Start
FROM openjdk:17

# 인자 설정 - JAR_FILE
ARG JAR_FILE=build/libs/*.jar

# jar File Copy
COPY ${JAR_FILE} mooluck-spring.jar

# ffmpeg 다운로드 및 설치
RUN wget -q -O /usr/bin/ffmpeg https://ffmpeg.org/releases/ffmpeg-4.4.tar.gz && \
    tar -xvf /usr/bin/ffmpeg -C /usr/bin/ && \
    chmod +x /usr/bin/ffmpeg && \
    ln -s /usr/bin/ffmpeg /usr/bin/ffprobe

ENV TZ=Asia/Seoul
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV FFPROBE_PATH=/usr/bin/ffprobe

# 기본 프로파일 실행 명령어
ENTRYPOINT ["java", "-jar", "mooluck-spring.jar", "-Dspring.profiles.active=docker", "-Duser.timezone=Asia/Seoul"]
