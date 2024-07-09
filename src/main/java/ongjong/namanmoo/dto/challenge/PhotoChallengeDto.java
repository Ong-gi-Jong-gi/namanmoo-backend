package ongjong.namanmoo.dto.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.answer.AnswerDto;

import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonPropertyOrder({ "challengeTitle", "challengeNumber", "challengeDate", "isComplete", "answerList" })
public class PhotoChallengeDto {
    private String challengeTitle;
    private String challengeNumber;
    private Long challengeDate;
    @JsonProperty("isComplete")
    private boolean isComplete;
    private List<AnswerDto> answerList;

    public PhotoChallengeDto(Challenge challenge, boolean isComplete, Long timeStamp , List<Answer> answers) {
        this.challengeTitle = challenge.getChallengeTitle();
        this.challengeNumber = challenge.getChallengeNum().toString();
        this.challengeDate = timeStamp; // Use actual challenge date if available
        this.isComplete = isComplete;
        this.answerList = answers.stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
    }

}
