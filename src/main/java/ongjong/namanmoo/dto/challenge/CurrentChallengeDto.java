package ongjong.namanmoo.dto.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import ongjong.namanmoo.domain.challenge.Challenge;

@Data
@JsonPropertyOrder({ "isDone", "challengeDto" })
public class CurrentChallengeDto {
    @JsonProperty("isDone")
    private boolean isDone;
    private ChallengeDto challengeDto;

    public CurrentChallengeDto(boolean isDone, ChallengeDto challengeDto) {
        this.isDone = isDone;
        this.challengeDto = challengeDto;
    }
    @Data
    public static class ChallengeDto {
        private String challengeId;
        private String challengeNumber;
        private String challengeTitle;
        private String challengeType;
        private String challengeDate;

        public ChallengeDto(Challenge challenge, Integer currentNum, String challengeDate) {
            this.challengeId = challenge.getChallengeId().toString();
            this.challengeNumber = currentNum.toString();
            this.challengeTitle = challenge.getChallengeTitle();
            this.challengeType = String.valueOf(challenge.getChallengeType());
            this.challengeDate = challengeDate;
        }
    }
}
