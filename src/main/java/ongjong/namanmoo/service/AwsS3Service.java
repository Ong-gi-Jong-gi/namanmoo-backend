package ongjong.namanmoo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
public class AwsS3Service {

    private final AmazonS3 amazonS3Client;
    private final String bucket;
    private final String region;

    /**
     * AwsS3Service 생성자.
     *
     * @param accessKeyId AWS 액세스 키 ID
     * @param secretKey   AWS 시크릿 액세스 키
     * @param bucket      S3 버킷 이름
     * @param region      AWS 리전
     */
    public AwsS3Service(
            @Value("${cloud.aws.credentials.access-key}") String accessKeyId,
            @Value("${cloud.aws.credentials.secret-key}") String secretKey,
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${cloud.aws.region.static}") String region) {

        // AWS 인증 정보 생성
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretKey);

        // AmazonS3 클라이언트 생성
        this.amazonS3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        // 필드 초기화
        this.bucket = bucket;
        this.region = region;
    }

    /**
     * MultipartFile을 S3에 업로드하고 업로드된 파일의 URL을 반환하는 메소드.
     *
     * @param multipartFile 업로드할 MultipartFile
     * @return 업로드된 파일의 URL
     * @throws IOException 파일 변환 또는 업로드 중 발생하는 예외
     */
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        log.debug("Converting MultipartFile to File...");
        File uploadFile = convertFile(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File convert fail"));

        String fileType = determineFileType(multipartFile);
        String fileName = generateFileName(uploadFile, fileType);

        log.debug("Uploading file to S3: {}", fileName);
        String uploadImageUrl = uploadFileToS3(uploadFile, fileName);
        log.debug("File uploaded to S3: {}", uploadImageUrl);

        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    /**
     * 파일 타입을 결정하는 메소드.
     *
     * @param multipartFile 파일
     * @return 파일 타입 (image/audio/video)
     */
    private String determineFileType(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        if (contentType != null && contentType.startsWith("image")) {
            return "image";
        } else if (contentType != null && contentType.startsWith("audio")) {
            return "audio";
        } else if (contentType != null && contentType.startsWith("video")) {
            return "video";
        }
        throw new IllegalArgumentException("Unsupported file type: " + contentType);
    }

    /**
     * MultipartFile을 File 객체로 변환하는 메소드.
     *
     * @param file 변환할 MultipartFile
     * @return Optional<File> 변환된 File 객체를 포함하는 Optional
     * @throws IOException 파일 변환 중 발생하는 예외
     */
    private Optional<File> convertFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File convertFile = new File(fileName);

        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }

    /**
     * 업로드될 파일의 고유한 파일 이름을 생성하는 메소드.
     *
     * @param uploadFile 업로드할 파일
     * @param fileType   파일 타입 (image/audio/video)
     * @return String 고유한 파일 이름
     */
    private String generateFileName(File uploadFile, String fileType) {
        return fileType + "/" + UUID.randomUUID() + "_" + uploadFile.getName();
    }

    /**
     * S3에 파일을 업로드하고 업로드된 파일의 URL을 반환하는 메소드.
     *
     * @param uploadFile 업로드할 파일
     * @param fileName   업로드할 파일의 이름
     * @return 업로드된 파일의 URL
     */
    private String uploadFileToS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );

        log.info("File Upload : " + fileName);

        return getS3FileURL(fileName);
    }

    /**
     * 업로드된 파일의 URL을 생성하는 메소드.
     *
     * @param fileName 파일 이름
     * @return 업로드된 파일의 URL
     */
    private String getS3FileURL(String fileName) {
        String defaultUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        return defaultUrl + fileName;
    }

    /**
     * 임시 파일을 삭제하는 메소드.
     *
     * @param targetFile 삭제할 파일
     */
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("File delete success");
        } else {
            log.info("File delete fail");
        }
    }

    /**
     * S3에서 파일을 삭제하는 메소드.
     *
     * @param fileName 삭제할 파일의 이름
     */
    public void delete(String fileName) {
        log.info("File Delete : " + fileName);
        amazonS3Client.deleteObject(bucket, fileName);
    }
}