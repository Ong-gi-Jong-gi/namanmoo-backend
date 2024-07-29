package ongjong.namanmoo.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;

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
        log.info("Converting MultipartFile to File...");
        File uploadFile = convertFile(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File convert fail"));

        // 이미지 파일의 경우 최적화
        if (determineFileType(multipartFile).equals("image")) {
            log.info("Optimizing image file...");
            uploadFile = optimizeImageFile(uploadFile);
        }

        String fileType = determineFileType(multipartFile);
        String fileName = generateFileName(uploadFile, fileType);

        log.info("Uploading file to S3: {}", fileName);
        String uploadFileUrl = uploadFileToS3(uploadFile, fileName);
        log.info("File uploaded to S3: {}", uploadFileUrl);

        removeNewFile(uploadFile);
        return uploadFileUrl;
    }

    /**
     * MultipartFile을 S3에 업로드하고 업로드된 파일의 URL을 반환하는 메소드.
     *
     * @param multipartFile 업로드할 MultipartFile
     * @return 업로드된 파일의 URL
     * @throws IOException 파일 변환 또는 업로드 중 발생하는 예외
     */
    public String uploadOriginalFile(MultipartFile multipartFile, Long memberId) throws IOException {
        log.info("Converting MultipartFile to File...");
        File uploadFile = convertFile(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File convert fail"));

        String fileType = determineFileType(multipartFile);
        String fileName = generateFileName(uploadFile, fileType, memberId);  // 멤버 ID 포함하여 파일 이름 생성

        log.info("Uploading file to S3: {}", fileName);
        String uploadFileUrl = uploadFileToS3(uploadFile, fileName);
        log.info("File uploaded to S3: {}", uploadFileUrl);

        removeNewFile(uploadFile);
        return uploadFileUrl;
    }

    /**
     * 이미지를 최적화하는 메소드.
     *
     * @param originalFile 최적화할 원본 이미지 파일
     * @return 최적화된 이미지 파일
     * @throws IOException 이미지 최적화 중 발생하는 예외
     */
    private File optimizeImageFile(File originalFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalFile);
        File optimizedFile = new File("optimized_" + originalFile.getName());

        // 원본 이미지의 크기
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 원하는 최대 크기
        int maxWidth = (int) (originalWidth * 0.85);
        int maxHeight = (int) (originalHeight * 0.85);

        // 비율 유지하면서 리사이징할 크기 계산
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth = maxWidth;
        int newHeight = (int) (maxWidth / aspectRatio);
        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = (int) (maxHeight * aspectRatio);
        }

        // 이미지 리사이즈 및 압축
        Thumbnails.of(originalImage)
                .size(newWidth, newHeight)  // 비율을 유지하면서 리사이즈
                .outputQuality(0.5)  // 이미지 품질 설정 (0.0 ~ 1.0)
                .toFile(optimizedFile);
        
        // 원본 이미지 파일의 크기
        long originalFileSize = originalFile.length();
        // 최적화된 이미지 파일의 크기
        long optimizedFileSize = optimizedFile.length();

        // 로그 출력
        log.info("Original Image Dimensions: {}x{}", originalWidth, originalHeight);
        log.info("Original File Size: {} bytes", originalFileSize);
        log.info("Optimized Image Dimensions: {}x{}", newWidth, newHeight);
        log.info("Optimized File Size: {} bytes", optimizedFileSize);


        return optimizedFile;
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
        } else if (contentType != null && contentType.startsWith("application")){
            return "application";
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
//        String fileName = file.getOriginalFilename();
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
     * @param fileType   파일 타입 (image/audio/video/application)
     * @return String 고유한 파일 이름
     */
    private String generateFileName(File uploadFile, String fileType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                .withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(Instant.now());

//        return fileType + "/" + UUID.randomUUID() + "_" + formattedDate + "_" + uploadFile.getName();
        return fileType + "/" + formattedDate + "_" + uploadFile.getName();
    }

    // generateFileName 메소드에 멤버 ID 추가
    private String generateFileName(File uploadFile, String fileType, Long memberId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                .withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(Instant.now());
        // 멤버 ID를 파일 이름에 포함시킵니다.
        return fileType + "/" + memberId + "_" + formattedDate + "_" + uploadFile.getName();
    }

    /**
     * S3에 파일을 업로드하고 업로드된 파일의 URL을 반환하는 메소드.
     *
     * @param uploadFile 업로드할 파일
     * @param fileName   업로드할 파일의 이름
     * @return 업로드된 파일의 URL
     */
    private String uploadFileToS3(File uploadFile, String fileName) {
        try {
            log.info("파일 업로드 시작: {}", fileName);

            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, fileName, uploadFile)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );

            log.info("파일 업로드 완료: {}", fileName);

            String fileUrl = getS3FileURL(fileName);
            log.info("S3에 파일 업로드 성공: {}", fileUrl);

            return fileUrl;
        } catch (AmazonServiceException e) {
            log.error("Amazon 서비스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (SdkClientException e) {
            log.error("SDK 클라이언트 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("알 수 없는 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
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

    // 업로드 실패 시 재시도 로직 추가
    public String uploadFileWithRetry(MultipartFile multipartFile, int retries) throws IOException, InterruptedException {
        File uploadFile = convertFile(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("Incorrect conversion from MultipartFile to File"));

        String fileType = determineFileType(multipartFile);
        String fileName = generateFileName(uploadFile, fileType);

        for (int i = 0; i < retries; i++) {
            try {
                amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead));

                String uploadedUrl = getS3FileURL(fileName);
                log.info("Successfully uploaded file to S3 on retry " + i + ": {}", uploadedUrl);

                removeNewFile(uploadFile); // Remember to clean up local file after upload
                return uploadedUrl;
            } catch (Exception ex) {
                log.warn("업로드에 실패하였습니다. 재시도하는중... (시도: {}/{})", i + 1, retries);
                Thread.sleep(3000);  // waiting for 3 seconds before the next retry
            }
        }
        throw new RuntimeException("Failed to upload file after " + retries + " retries.");
    }

    // 오디오 파일 고정 경로 생성
    public String uploadAudioFile(MultipartFile multipartFile, String s3Path) throws IOException {
        log.info("Converting MultipartFile to File...");
        File uploadFile = convertFile(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File convert fail"));
        String fileName = multipartFile.getOriginalFilename();

        log.info("Uploading file to S3: {}", fileName);
        String uploadFileUrl = uploadFileToS3(uploadFile, s3Path);
        log.info("File uploaded to S3: {}", uploadFileUrl);

        removeNewFile(uploadFile);
        return uploadFileUrl;
    }

    // 오디오 파일 목록을 S3에서 조회하여 반환
    public List<String> listAudioFiles(Long luckyId) {
        // 럭키 아이디에 따라 특정 디렉토리 설정
        String prefix = "split-audio/럭키_" + luckyId + "/";

        // S3 버킷에서 특정 프리픽스를 가지는 객체들을 나열하기 위한 요청 객체 생성
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(prefix);
        // 각 객체 목록 요청을 실행하고 결과를 받음
        ListObjectsV2Result result;
        result = amazonS3Client.listObjectsV2(req);

        // 결과에서 객체 목록을 추출하여 각 객체의 키를 리스트로 변환
        return result.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    /**
     * S3에서 파일을 다운로드하여 로컬 파일로 저장하는 메소드.
     *
     * @param s3Path    S3 경로
     * @param localFile 로컬 파일 객체
     */
    public void downloadFile(String s3Path, File localFile) {
        try (S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucket, s3Path));
             InputStream inputStream = s3Object.getObjectContent();
             FileOutputStream outputStream = new FileOutputStream(localFile)) {

            byte[] readBuf = new byte[1024];
            int readLen;
            while ((readLen = inputStream.read(readBuf)) > 0) {
                outputStream.write(readBuf, 0, readLen);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

}