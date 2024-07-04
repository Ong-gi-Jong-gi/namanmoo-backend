package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.domain.challenge.NormalC;

@Data
public class ChallengeDto {
    private Long challengeId;
    private Long challengeNumber;
    private String challengeTitle;
    private String challengeType;

    public ChallengeDto(Challenge challenge) {
        this.challengeId = challenge.getChallengeId();
        this.challengeNumber = challenge.getChallengeNum();
        this.challengeTitle = ((NormalC) challenge).getNormalChallenge();
        this.challengeType = challenge.getClass().getSimpleName();
    }
}
