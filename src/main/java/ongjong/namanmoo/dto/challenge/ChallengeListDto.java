package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.*;

@Data
public class ChallengeListDto {
    private Long challengeId;
    private Long challengeNumber;
    private String challengeTitle1;     // group
    private String challengeTitle2 = null;
    private String challengeType;
    private boolean isComplete;

    public ChallengeListDto(Challenge challenge, boolean isComplete) {
        this.challengeId = challenge.getChallengeId();
        this.challengeNumber = challenge.getChallengeNum();
        if (challenge instanceof NormalC) {
            this.challengeTitle1 = ((NormalC) challenge).getNormalChallenge();
        }
        else if (challenge instanceof GroupC) {
            this.challengeTitle1 = ((GroupC) challenge).getParentChallenge();
            this.challengeTitle2 = ((GroupC) challenge).getChildChallenge();
        }
        else if (challenge instanceof FaceTimeC) {
            this.challengeTitle1 = ((FaceTimeC) challenge).getFaceTimeChallenge();
        }
        else if (challenge instanceof PhotoC) {
            this.challengeTitle1 = ((PhotoC) challenge).getPhotoChallenge();
        }
        else if (challenge instanceof VoiceC) {
            this.challengeTitle1 = ((VoiceC) challenge).getVoiceChallenge();
        }
        this.challengeType = challenge.getClass().getSimpleName();
        this.isComplete = isComplete;
    }
}
