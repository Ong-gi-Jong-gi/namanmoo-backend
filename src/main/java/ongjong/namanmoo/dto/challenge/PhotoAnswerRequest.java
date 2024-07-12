package ongjong.namanmoo.dto.challenge;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class PhotoAnswerRequest {
    private Long challengeId;
    private MultipartFile answer;
}
