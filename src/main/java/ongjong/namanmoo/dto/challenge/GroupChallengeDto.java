package ongjong.namanmoo.dto.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.domain.challenge.ChallengeType;
import ongjong.namanmoo.dto.answer.AnswerDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Data
@Slf4j
@JsonPropertyOrder({ "challengeNumber", "challengeDate", "isComplete", "challengeTitle", "answerList" })
public class GroupChallengeDto {
    private String challengeNumber;
    private Long challengeDate;
    @JsonProperty("isComplete")
    private boolean isComplete;
    private NewChallengeDto parentChallenge;
    private NewChallengeDto childrenChallenge;

    public GroupChallengeDto(Challenge challenge , Long timeStamp, boolean isComplete, List<Answer> answers) {
        this.challengeNumber = challenge.getChallengeNum().toString();
        this.challengeDate = timeStamp;
        this.isComplete = isComplete;
        List<Answer> parentAnswerList = new ArrayList<>();      // 부모 answer 리스트
        List<Answer> childAnswerList = new ArrayList<>();       // 자식 answer 리스트

        for (Answer answer : answers) {
            if (answer.getChallenge().getChallengeType() == ChallengeType.GROUP_PARENT) {
                parentAnswerList.add(answer);
            } else {
                childAnswerList.add(answer);
            }
        }

        List<AnswerDto> parentAnswerDtoList = parentAnswerList.stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
        if (!parentAnswerList.isEmpty()) {
            this.parentChallenge = new NewChallengeDto(parentAnswerList.get(0).getChallenge().getChallengeTitle(), parentAnswerDtoList);
        } else {
            this.parentChallenge = new NewChallengeDto("No Parent Challenge", new ArrayList<>());
        }

        List<AnswerDto> childAnswerDtoList = childAnswerList.stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
        if (!childAnswerList.isEmpty()) {
            this.childrenChallenge = new NewChallengeDto(childAnswerList.get(0).getChallenge().getChallengeTitle(), childAnswerDtoList);
        } else {
            this.childrenChallenge = new NewChallengeDto("No Children Challenge", new ArrayList<>());
        }
    }

    @Data
    @JsonPropertyOrder({"challengeTitle", "answerList"})
    private static class NewChallengeDto {
        @JsonProperty("challengeTitle")
        private String challengeTitle;

        @JsonProperty("answerList")
        private List<AnswerDto> answerList;

        NewChallengeDto(String challengeTitle, List<AnswerDto> answerList) {
            this.challengeTitle = challengeTitle;
            this.answerList = answerList;
        }
    }
}
