package ongjong.namanmoo.dto.challenge;

import lombok.Data;
import ongjong.namanmoo.domain.answer.Answer;

@Data
public class AnswerDto {
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