package ongjong.namanmoo.dto.recapMember;

import lombok.Data;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;

@Data
public class MemberAndCountDto {
    private String userId;
    private String nickname;
    private String userImg;
    private String role;
    private int count;

    public MemberAndCountDto(Member member, int count) {
        this.userId = member.getLoginId();
        this.nickname = member.getNickname();
        this.userImg = member.getMemberImage();
        this.role = member.getRole();
        this.count = count;
    }
}
