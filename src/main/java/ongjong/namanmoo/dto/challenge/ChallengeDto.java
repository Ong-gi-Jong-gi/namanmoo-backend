package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.challenge.Challenge;

import java.util.List;


@Data
public class ChallengeDto {
    private String challengeId;
    private String challengeNumber;
    private String challengeTitle;
    private String challengeType;

    public ChallengeDto(Challenge challenge, Long currentNum) {
        this.challengeId = challenge.getChallengeId().toString();
        this.challengeNumber = currentNum.toString();
        this.challengeTitle = challenge.getChallengeTitle();
        this.challengeType = String.valueOf(challenge.getChallengeType());
    }
}
