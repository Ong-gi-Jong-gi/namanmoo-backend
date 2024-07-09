package ongjong.namanmoo.dto.answer;

import lombok.Data;
import ongjong.namanmoo.domain.MemberRole;
import ongjong.namanmoo.domain.answer.Answer;

@Data
public class AnswerDto {
    private String userId;
    private String userImg;
    private String answer;
    private String nickname;
    private String role;

    public AnswerDto(Answer answer) {
        this.userId = answer.getMember().getMemberId().toString();
        this.userImg = answer.getMember().getMemberImage();
        this.answer = answer.getAnswerContent();
        this.nickname = answer.getMember().getNickname();
        this.role = answer.getMember().getRole();
    }
}