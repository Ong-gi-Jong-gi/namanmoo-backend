package ongjong.namanmoo.dto.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import ongjong.namanmoo.domain.challenge.Challenge;

@Data
@JsonPropertyOrder({ "isDone", "challengeInfo" })
public class CurrentChallengeDto {
    @JsonProperty("isDone")
    private boolean isDone;
    private ChallengeInfo challengeInfo;

    public CurrentChallengeDto(boolean isDone, ChallengeInfo challengeInfo) {
        this.isDone = isDone;
        this.challengeInfo = challengeInfo;
    }
    @Data
    public static class ChallengeInfo {
        private String challengeId;
        private String challengeNumber;
        private String challengeTitle;
        private String challengeType;
        private String challengeDate;

        public ChallengeInfo(Challenge challenge, Integer currentNum, String challengeDate) {
            this.challengeId = challenge.getChallengeId().toString();
            this.challengeNumber = currentNum.toString();
            this.challengeTitle = challenge.getChallengeTitle();
            this.challengeType = String.valueOf(challenge.getChallengeType());
            this.challengeDate = challengeDate;
        }
    }
}
