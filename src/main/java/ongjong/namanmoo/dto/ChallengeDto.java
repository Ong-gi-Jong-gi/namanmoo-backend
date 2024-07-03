package ongjong.namanmoo.dto;

import lombok.Data;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.domain.challenge.NormalC;

@Data
public class ChallengeDto {
    private Long challengeId;
    private Long challengeNumber;
    private String challengeTitle;

    public ChallengeDto(Challenge challenge) {
        this.challengeId = challenge.getChallengeId();
        this.challengeNumber = challenge.getChallengeNum();
        this.challengeTitle = ((NormalC) challenge).getNormalChallenge();
    }
}
