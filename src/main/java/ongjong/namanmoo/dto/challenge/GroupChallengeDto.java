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
@JsonPropertyOrder({ "challengeNumber", "challengeDate", "isComplete", "parentChallenge", "childrenChallenge" })
public class GroupChallengeDto {
    private String challengeNumber;
    private Long challengeDate;
    @JsonProperty("isComplete")
    private boolean isComplete;
    private NewChallengeDto parentChallenge;
    private NewChallengeDto childrenChallenge;

    public GroupChallengeDto(String challengeNumber, Long challengeDate, boolean isComplete,
                             NewChallengeDto parentChallenge, NewChallengeDto childrenChallenge) {
        this.challengeNumber = challengeNumber;
        this.challengeDate = challengeDate;
        this.isComplete = isComplete;
        this.parentChallenge = parentChallenge;
        this.childrenChallenge = childrenChallenge;
    }

    @Data
    @JsonPropertyOrder({"challengeTitle", "answerList"})
    public static class NewChallengeDto {
        @JsonProperty("challengeTitle")
        private String challengeTitle;
        @JsonProperty("answerList")
        private List<AnswerDto> answerList;

        public NewChallengeDto(String challengeTitle, List<AnswerDto> answerList) {
            this.challengeTitle = challengeTitle;
            this.answerList = answerList;
        }
    }


//    public static GroupChallengeDto from(Challenge challenge, Long timeStamp, boolean isComplete, List<Answer> answers) {
//        List<Answer> parentAnswerList = new ArrayList<>();
//        List<Answer> childAnswerList = new ArrayList<>();
//
//        for (Answer answer : answers) {
//            if (answer.getChallenge().getChallengeType() == ChallengeType.GROUP_PARENT) {
//                parentAnswerList.add(answer);
//            } else {
//                childAnswerList.add(answer);
//            }
//        }
//
//        List<AnswerDto> parentAnswerDtoList = parentAnswerList.stream()
//                .map(AnswerDto::new)
//                .collect(Collectors.toList());
//        NewChallengeDto parentChallenge = parentAnswerList.isEmpty() ?
//                new NewChallengeDto("No Parent Challenge", new ArrayList<>()) :
//                new NewChallengeDto(parentAnswerList.get(0).getChallenge().getChallengeTitle(), parentAnswerDtoList);
//
//        List<AnswerDto> childAnswerDtoList = childAnswerList.stream()
//                .map(AnswerDto::new)
//                .collect(Collectors.toList());
//        NewChallengeDto childrenChallenge = childAnswerList.isEmpty() ?
//                new NewChallengeDto("No Children Challenge", new ArrayList<>()) :
//                new NewChallengeDto(childAnswerList.get(0).getChallenge().getChallengeTitle(), childAnswerDtoList);
//
//        return new GroupChallengeDto(challenge.getChallengeNum().toString(), timeStamp, isComplete, parentChallenge, childrenChallenge);
//    }
}
