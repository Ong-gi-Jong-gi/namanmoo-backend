package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.answer.NormalA;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.domain.challenge.NormalC;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class NormalChallengeDto {
    private Long challengeId;
    private Long challengeNumber;
    private String challengeTitle;
    private String challengeType;
    private boolean isComplete;
    private Timestamp challengeDate;
    private List<AnswerDto> answerList;

    public NormalChallengeDto(Challenge challenge, boolean isComplete, List<Answer> answers) {
        this.challengeId = challenge.getChallengeId();
        this.challengeNumber = challenge.getChallengeNum();
        this.challengeTitle = ((NormalC) challenge).getNormalChallenge();
        this.challengeType = challenge.getClass().getSimpleName(); // or challenge.getChallengeType();
        this.isComplete = isComplete;
        this.challengeDate = new Timestamp(System.currentTimeMillis()); // Use actual challenge date if available
        this.answerList = answers.stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());

    }

    @Data
    private static class AnswerDto {
        private String userId;
        private String userImg;
        private String answer;
        private String nickname;
        private String role;

        public AnswerDto(Answer answer) {
            this.userId = answer.getMember().getLoginId();
            this.userImg = answer.getMember().getMemberImage();
            this.answer = ((NormalA) answer).getNormalAnswer();
            this.nickname = answer.getMember().getNickname();
            this.role = answer.getMember().getRole();
        }
    }
}
