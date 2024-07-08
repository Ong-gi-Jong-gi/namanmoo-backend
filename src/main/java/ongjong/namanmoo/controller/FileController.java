package ongjong.namanmoo.controller;
import java.io.IOException;

import ongjong.namanmoo.service.AwsS3Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class FileController {
    private final AwsS3Service awsS3Service;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        return AwsS3Service.uploadFile(multipartFile);
    }
}