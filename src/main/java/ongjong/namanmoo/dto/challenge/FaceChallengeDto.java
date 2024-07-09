package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.answer.AnswerDto;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class FaceChallengeDto {
    private String challengeTitle;
    private String challengeNumber;
    private Long challengeDate;
    private boolean isComplete;
    private String code;

    public FaceChallengeDto(Challenge challenge, Long timeStamp, boolean isComplete, String inviteCode) {
        this.challengeTitle = challenge.getChallengeTitle();
        this.challengeNumber = challenge.getChallengeNum().toString();
        this.challengeDate = timeStamp;
        this.isComplete = isComplete;
        this.code = inviteCode;
    }
}
