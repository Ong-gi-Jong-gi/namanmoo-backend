package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;


import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public class NormalChallengeDto {
    private String challengeTitle;
    private String challengeNumber;
    private Long challengeDate;
    private boolean isComplete;
    private List<AnswerDto> answerList;

    public NormalChallengeDto(Challenge challenge, boolean isComplete, Long timeStamp , List<Answer> answers) {
        this.challengeTitle = challenge.getChallengeTitle();
        this.challengeNumber = challenge.getChallengeNum().toString();
        this.challengeDate = timeStamp; // Use actual challenge date if available
        this.isComplete = isComplete;
        this.answerList = answers.stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
    }

    private List<AnswerDto> answerDto;
}
