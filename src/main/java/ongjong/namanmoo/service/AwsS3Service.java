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

    public AwsS3Service(
            @Value("${aws.accessKeyId}") String accessKeyId,
            @Value("${aws.secretKey}") String secretKey,
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${aws.region}") String region) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretKey);
        this.amazonS3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        this.bucket = bucket;
    }

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        log.debug("Converting MultipartFile to File...");
        File uploadFile = convertFile(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File convert fail"));

        String fileName = generateFileName(uploadFile);

        log.debug("Uploading file to S3: {}", fileName);
        String uploadImageUrl = uploadFileToS3(uploadFile, fileName);
        log.debug("File uploaded to S3: {}", uploadImageUrl);

        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

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

    private String generateFileName(File uploadFile) {
        return UUID.randomUUID() + "_" + uploadFile.getName();
    }

    private String uploadFileToS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );

        log.info("File Upload : " + fileName);

        return getS3FileURL(fileName);
    }

    private String getS3FileURL(String fileName) {
        String defaultUrl = "https://s3.amazonaws.com/";
        return defaultUrl + bucket + "/" + fileName;
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("File delete success");
        } else {
            log.info("File delete fail");
        }
    }

    public void delete(String fileName) {
        log.info("File Delete : " + fileName);
        amazonS3Client.deleteObject(bucket, fileName);
    }
}
