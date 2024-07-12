package ongjong.namanmoo.dto.challenge;

import lombok.AllArgsConstructor;
import lombok.Data;
import ongjong.namanmoo.domain.answer.Answer;

import java.util.List;

@Data
@AllArgsConstructor
public class ChallengeDetailsDto {
    private Long challengeDate;
    private boolean isComplete;
    private List<Answer> answers;
}
