package ongjong.namanmoo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class AwsS3Service {

    private static AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private static String bucket;

    public static String upload(MultipartFile multipartFile) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File convert fail"));

        String fileName = generateFileName(uploadFile);

        String uploadImageUrl = uploadFileToS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    private static Optional<File> convert(MultipartFile file) throws IOException {
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

    private static String generateFileName(File uploadFile) {
        return UUID.randomUUID() + "_" + uploadFile.getName();
    }

    private static String uploadFileToS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );

        log.info("File Upload : " + fileName);

        return getS3FileURL(fileName);
    }

    private static String getS3FileURL(String fileName) {
        String defaultUrl = "https://s3.amazonaws.com/";
        return defaultUrl + bucket + "/" + fileName;
    }

    private static void removeNewFile(File targetFile) {
        if (!targetFile.delete()) {
            log.error("File delete fail: " + targetFile.getName() + " at path: " + targetFile.getAbsolutePath());
        }
    }

    public void delete(String fileName) {
        log.info("File Delete : " + fileName);
        amazonS3Client.deleteObject(bucket, fileName);
    }
}
