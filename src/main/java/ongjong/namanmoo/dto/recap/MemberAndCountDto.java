package ongjong.namanmoo.dto.recap;

import lombok.Data;
import ongjong.namanmoo.domain.Member;

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
