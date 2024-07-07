package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class NormalChallengeDto {
    private String challengeTitle;
    private String challengeNumber;
    private Long challengeDate;
    private boolean isComplete;
    private List<AnswerDto> answerList;

    public NormalChallengeDto(Challenge challenge, boolean isComplete, Long timeStamp , List<Answer> answers) {
        this.challengeTitle = challenge.getChallengeTitle();
        this.challengeNumber = challenge.getChallengeNum().toString();
        this.challengeDate = timeStamp; // Use actual challenge date if available
        this.isComplete = isComplete;
        this.answerList = answers.stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
    }

    @Data
    private static class AnswerDto {
        private String memberId;
        private String userImg;
        private String answer;
        private String nickname;
        private String role;

        public AnswerDto(Answer answer) {
            this.memberId = answer.getMember().getMemberId().toString();
            this.userImg = answer.getMember().getMemberImage();
            this.answer = answer.getAnswerContent();
            this.nickname = answer.getMember().getNickname();
            this.role = answer.getMember().getRole();
        }
    }
}
