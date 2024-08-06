# Mooluck - Backend

## 요구사항 및 의존성

- Java 17
- Spring Boot 3.3.1
- Spring Boot Data JPA
- Lombok
- MySQL 8.0.37
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
- MySQL을 설치합니다. 
  AWS RDS 또는 로컬 MySQL 인스턴스를 사용할 수 있습니다.
- MySQL에서 애플리케이션용 스키마를 생성합니다:
  ```sql
  CREATE SCHEMA your-database-name;
  ```
- `application.properties` 또는 `application.yml` 파일에 다음 정보를 추가합니다:
  ```properties
  spring.datasource.url=jdbc:mysql://your-database-url:3306/your-database-name
  spring.datasource.username=your-database-username
  spring.datasource.password=your-database-password

  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.properties.hibernate.format_sql=true
  spring.jpa.properties.hibernate.default_batch_fetch_size=100
  ```
  - 예시로 로컬 MySQL을 사용하는 경우:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/your-database-name
    spring.datasource.username=your-local-database-username
    spring.datasource.password=your-local-database-password
    ```

  - 예시로 AWS RDS를 사용하는 경우:
    ```properties
    spring.datasource.url=jdbc:mysql://your-rds-endpoint:3306/your-database-name
    spring.datasource.username=your-rds-database-username
    spring.datasource.password=your-rds-database-password
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

### 8. Docker 설정

#### 8.1 Docker 설치
- Docker를 설치하고 Docker 데몬이 실행 중인지 확인합니다. 설치 방법은 Docker 공식 문서를 참고하세요:
  - [Docker for Windows](https://docs.docker.com/docker-for-windows/install/)
  - [Docker for Mac](https://docs.docker.com/docker-for-mac/install/)
  - [Docker for Linux](https://docs.docker.com/engine/install/)

#### 8.2 Docker 유저 설정
- Docker를 사용하려면 Docker 데몬에 대한 권한이 필요합니다. 현재 사용자를 Docker 그룹에 추가하여 권한을 부여할 수 있습니다:
  ```sh
  sudo usermod -aG docker $USER
  ```
  - 이 명령어를 실행한 후, 로그아웃하고 다시 로그인하여 변경 사항을 적용합니다.

#### 8.3 Docker 이미지 빌드 및 실행
- 프로젝트 루트 디렉토리에 있는 Dockerfile을 사용하여 Docker 이미지를 빌드합니다:
  ```sh
  docker build -t your-docker-image-name .
  ```
- Docker 컨테이너를 실행합니다:
  ```sh
  docker run -d -p 8080:8080 your-docker-image-name
  ```

#### 8.4 Docker 환경 변수 설정
- Docker 컨테이너 실행 시 필요한 환경 변수를 설정합니다. 예를 들어, MySQL 데이터베이스와 연동하려면 다음과 같이 환경 변수를 지정할 수 있습니다:
  ```sh
  docker run -d -p 8080:8080 \
    -e SPRING_DATASOURCE_URL="jdbc:mysql://your-database-url:3306/your-database-name" \
    -e SPRING_DATASOURCE_USERNAME="your-database-username" \
    -e SPRING_DATASOURCE_PASSWORD="your-database-password" \
    -e SECURITY_JWT_TOKEN_SECRET_KEY="your-jwt-secret-key" \
    your-docker-image-name
  ```

#### 8.5 Docker 이미지 및 컨테이너 관리
- 실행 중인 Docker 컨테이너 목록을 확인하려면 다음 명령어를 사용합니다:
  ```sh
  docker ps
  ```
- 특정 컨테이너의 로그를 확인하려면 다음 명령어를 사용합니다:
  ```sh
  docker logs <container_id>
  ```
- 컨테이너를 중지하려면 다음 명령어를 사용합니다:
  ```sh
  docker stop <container_id>
  ```
- 컨테이너를 삭제하려면 다음 명령어를 사용합니다:
  ```sh
  docker rm <container_id>
  ```
- 사용하지 않는 Docker 이미지를 삭제하려면 다음 명령어를 사용합니다:
  ```sh
  docker rmi $(docker images -q)
  ```

#### 8.6 CloudWatch Logs 연동

**Docker와 CloudWatch 연동**

Docker 컨테이너의 로그를 CloudWatch Logs로 직접 전송할 수 있습니다. 이를 위해 `awslogs` 로그 드라이버를 사용할 수 있습니다.

- **Docker 로그 드라이버 설정**:
  Docker 컨테이너 실행 시 `--log-driver` 옵션을 사용하여 CloudWatch Logs에 로그를 전송합니다:
  ```sh
  docker run -d -p 8080:8080 \
    --log-driver=awslogs \
    --log-opt awslogs-group=your-log-group \
    --log-opt awslogs-stream=your-log-stream \
    your-docker-image-name
  ```
  이 방법을 사용하면 Docker 로그가 직접 CloudWatch Logs로 전송되어, 중앙에서 로그를 관리하고 모니터링할 수 있습니다.

- **CloudWatch Logs 설정**:
  - **IAM 역할 설정**: CloudWatch Logs에 로그를 전송하려면 IAM 역할이 필요합니다. CloudWatchLogsFullAccess 권한을 가진 IAM 역할을 EC2 인스턴스에 부여합니다.
  - **CloudWatch Logs 에이전트 설치 및 설정**: 
    1. **에이전트 설치**: EC2 인스턴스에서 CloudWatch Logs 에이전트를 설치합니다. 설치 방법은 [AWS CloudWatch Logs 에이전트 문서](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/QuickStartEC2Instance.html)를 참조하세요.
    2. **설정 파일 수정**: 에이전트 설정 파일을 수정하여 로그 파일 경로와 로그 그룹을 지정합니다.
    3. **에이전트 시작**: 설정이 완료되면 에이전트를 시작하여 로그를 CloudWatch로 전송합니다.

이와 같이 CloudWatch와 Docker를 연동하면 로그를 중앙에서 수집하고 모니터링할 수 있어, 시스템 운영 및 문제 해결에 유용합니다.

### CI/CD 설정

이 프로젝트는 GitHub Actions를 사용하여 CI/CD 파이프라인을 설정하였습니다. 변경 사항이 main 또는 dev 브랜치에 푸시되거나 풀 리퀘스트가 발생할 때 자동으로 빌드 및 배포가 실행됩니다.

#### 주요 단계

- `build` : 프로젝트를 빌드하고 Docker 이미지를 생성합니다.
- `deploy` : 빌드한 Docker 이미지를 AWS EC2 인스턴스에 배포합니다.

#### CI/CD 파이프라인 설정 예시
- `.github/workflows/Spring Boot CI-CD.yml` 파일에 CI/CD 파이프라인 설정이 포함되어 있습니다. 이 파일을 참고하여 설정을 조정할 수 있습니다.

### 환경 변수 및 GitHub Secrets 정보 설정

GitHub Secrets에 다음 정보를 설정해야 합니다:
- **DOCKERHUB_USERNAME**: DockerHub 계정 이름
- **DOCKERHUB_PASSWORD**: DockerHub 계정 비밀번호
- **PROJECT_NAME**: 프로젝트 이름
- **EC2_HOST**: EC2 호스트 주소
- **EC2_USER**: EC2 사용자명
- **EC2_SSH_KEY**: EC2 SSH 프라이빗 키
- **DB_URL**: 데이터베이스 접속 URL
- **DB_USERNAME**: 데이터베이스 사용자명
- **DB_PASSWORD**: 데이터베이스 비밀번호
- **JWT_SECRET_KEY**: JWT 비밀 키
- **S3_ACCESS_KEY_ID**: AWS S3 접근 키
- **S3_SECRET_ACCESS_KEY**: AWS S3 비밀 키
- **S3_BUCKET_NAME**: AWS S3 버킷 이름
- **S3_REGION**: AWS S3 리전
- **OPENAI_API_KEY**: OpenAI API 키
- **FFMPEG_PATH**: FFmpeg 경로
- **FFPROBE_PATH**: FFprobe 경로
- **AWS_REGION**: AWS 리전
- **LOG_GROUP_NAME**: AWS CloudWatch 로그 그룹 이름
- **LOG_STREAM_NAME**: AWS CloudWatch 로그 스트림 이름

자세한 정보는 `.github/workflows/Spring Boot CI-CD.yml` 파일을 참고해 주세요.

<!--
## 기여

- 기여는 언제나 환영합니다! 저장소를 포크하고, 개선 사항이나 버그 수정을 위한 풀 리퀘스트를 제출해 주세요.

## 라이선스

- 이 프로젝트는 MIT 라이선스를 따릅니다. 자세한 내용은 `LICENSE` 파일을 참조하십시오.
-->
