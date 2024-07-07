package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.*;

@Data
public class ChallengeListDto {
    private String challengeId;
    private String challengeNumber;
    private String challengeTitle;
    private String challengeType;
    private boolean isComplete;

    public ChallengeListDto(Challenge challenge, boolean isComplete) {
        this.challengeId = challenge.getChallengeId().toString();
        this.challengeNumber = challenge.getChallengeNum().toString();
        this.challengeTitle = challenge.getChallengeTitle();
        this.challengeType = challenge.getClass().getSimpleName();
        this.isComplete = isComplete;
    }
}
