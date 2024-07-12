package ongjong.namanmoo.dto.challenge;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveAnswerRequest {
    private Long challengeId;
    private String answerContent;
}
