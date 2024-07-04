package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.domain.challenge.NormalC;

@Data
public class ChallengeListDto {
    private Long challengeId;
    private Long challengeNumber;
    private String challengeTitle;
    private String challengeType;
    private boolean isComplete;

    public ChallengeListDto(Challenge challenge, boolean isComplete) {
        this.challengeId = challenge.getChallengeId();
        this.challengeNumber = challenge.getChallengeNum();
        this.challengeTitle = ((NormalC) challenge).getNormalChallenge();
        this.challengeType = challenge.getClass().getSimpleName(); // TODO: 타입이 제대로 나오니
        this.isComplete = isComplete;
    }
}
