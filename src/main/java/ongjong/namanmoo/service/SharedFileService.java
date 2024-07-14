package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.FileType;
import ongjong.namanmoo.domain.SharedFile;
import ongjong.namanmoo.repository.SharedFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SharedFileService {

    private final SharedFileRepository sharedFileRepository;
    private final AwsS3Service awsS3Service;

    public Map<String, String> uploadImageFile(int challengeNum, MultipartFile photo, FileType fileType) throws IOException {

        Map<String, String> response = new HashMap<>();

        if (fileType != FileType.IMAGE) {
            throw new IllegalArgumentException("Invalid file type: " + fileType);
        }

        // S3에 파일 업로드 및 URL 저장
        String uploadedUrl = awsS3Service.uploadFile(photo);

        // SharedFile 엔티티 저장
        SharedFile sharedFile = new SharedFile();
        sharedFile.setFileName(uploadedUrl);
        sharedFile.setFileType(FileType.IMAGE);
        sharedFile.setChallengeNum(challengeNum);
        sharedFile.setCreationDate(System.currentTimeMillis());
        sharedFileRepository.save(sharedFile);

        response.put("url", uploadedUrl);
        response.put("message", "Photo uploaded successfully");

        return response;
    }

}
