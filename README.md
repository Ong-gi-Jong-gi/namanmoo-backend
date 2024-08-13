# Mooluck - backend

## 요구사항 및 의존성

- Java 17
- Spring Boot 3.3.1
- Spring Boot Data JPA
- Lombok
- MySQL
- JSON Web Token (JWT)
- AWS SDK (S3 포함)
- FFmpeg
- 기타 의존성은 `build.gradle` 파일을 참조하십시오.

## 설치 가이드

### 1. 저장소 클론
```sh
git clone https://github.com/Ong-gi-Jong-gi/namanmoo-backend.git
cd namanmoo-backend
```

### 2. Java 설치 및 설정
- Java JDK 17을 설치합니다.
- `JAVA_HOME` 환경 변수를 설정합니다.

### 3. IDE 설정
- IntelliJ IDEA를 사용하는 것을 권장합니다.
- IntelliJ IDEA에서 프로젝트를 열고 Gradle 의존성을 자동으로 불러오도록 합니다.

### 4. 데이터베이스 설정
- MySQL을 설치하고 데이터베이스를 생성합니다.
- `application.properties` 또는 `application.yml` 파일에 다음 정보를 추가합니다:
  ```properties
  spring.datasource.url=jdbc:mysql://your-database-url:3306/your-database-name
  spring.datasource.username=your-database-username
  spring.datasource.password=your-database-password
  ```

### 5. AWS S3 설정
- AWS S3에 대한 접근 키와 비밀 키를 환경 변수 또는 `application.yml` 파일에 추가합니다:
  ```yaml
  cloud:
    aws:
      credentials:
        access-key: your-access-key-id
        secret-key: your-secret-access-key
      s3:
        bucket: your-bucket-name
      region:
        static: your-region
  ```

### 6. OpenAI API 설정
- OpenAI API 키를 환경 변수 또는 `application.yml` 파일에 추가합니다:
  ```yaml
  openai-service:
    api-key: your-openai-api-key
    gpt-model: gpt-3.5-turbo
    audio-model: whisper-1
    http-client:
      read-timeout: 3000
      connect-timeout: 3000
    urls:
      base-url: https://api.openai.com/v1
      chat-url: /chat/completions
      create-transcription-url: /audio/transcriptions
  ```

### 7. FFmpeg 설정
- FFmpeg와 FFprobe 경로를 환경 변수 또는 `application.yml` 파일에 추가합니다:
  ```yaml
  ffmpeg:
    path: path-to-your-ffmpeg
  ffprobe:
    path: path-to-your-ffprobe
  ```

## 사용 가이드

- 애플리케이션을 시작하려면 다음 명령어를 실행합니다:
  ```sh
  ./gradlew bootRun
  ```
- 브라우저에서 `http://localhost:8080`을 열어 서비스를 확인합니다.

## 테스트

- 테스트를 실행하려면 다음 명령어를 사용합니다:
  ```sh
  ./gradlew test
  ```

## CI/CD 설정

이 프로젝트는 GitHub Actions를 사용하여 CI/CD 파이프라인을 설정하였습니다. 변경 사항이 main 또는 dev 브랜치에 푸시되거나 풀 리퀘스트가 발생할 때 자동으로 빌드 및 배포가 실행됩니다.

### CI 파이프라인의 주요 단계

- `build`: 프로젝트를 빌드하고 Docker 이미지를 생성
- `deploy`: 빌드한 Docker 이미지를 AWS EC2 인스턴스에 배포

### 환경 변수 및 GitHub Secrets 정보 설정

GitHub Secrets에 다음 정보를 설정해야 합니다:
- DOCKERHUB_USERNAME: DockerHub 계정 이름
- DOCKERHUB_PASSWORD: DockerHub 계정 비밀번호
- PROJECT_NAME: 프로젝트 이름
- EC2_HOST: EC2 호스트 주소
- EC2_USER: EC2 사용자명
- EC2_SSH_KEY: EC2 SSH 프라이빗 키
- DB_URL: 데이터베이스 접속 URL
- DB_USERNAME: 데이터베이스 사용자명
- DB_PASSWORD: 데이터베이스 비밀번호
- JWT_SECRET_KEY: JWT 비밀 키
- S3_ACCESS_KEY_ID: AWS S3 접근 키
- S3_SECRET_ACCESS_KEY: AWS S3 비밀 키
- S3_BUCKET_NAME: AWS S3 버킷 이름
- S3_REGION: AWS S3 리전
- OPENAI_API_KEY: OpenAI API 키
- FFMPEG_PATH: FFmpeg 경로
- FFPROBE_PATH: FFprobe 경로
- AWS_REGION: AWS 리전
- LOG_GROUP_NAME: AWS Cloudwatch 로그 그룹 이름
- LOG_STREAM_NAME: AWS Cloudwatch 로그 스트림 이름

자세한 정보는 `.github/workflows/Spring Boot CI-CD.yml` 파일을 참고해주시길 바랍니다.

<!--
## 기여

- 기여는 언제나 환영합니다! 저장소를 포크하고, 개선 사항이나 버그 수정을 위한 풀 리퀘스트를 제출해 주세요.

## 라이선스

- 이 프로젝트는 MIT 라이선스를 따릅니다. 자세한 내용은 `LICENSE` 파일을 참조하십시오.
-->
