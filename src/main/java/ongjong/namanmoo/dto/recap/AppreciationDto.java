package ongjong.namanmoo.dto.recap;

import lombok.Data;
import ongjong.namanmoo.domain.Member;

@Data
public class AppreciationDto {
    private String userId;
    private String nickname;
    private String userImg;
    private String role;
    private String thanks;
    private String sorry;

    public AppreciationDto(Member member, String thanks, String sorry) {
        this.userId = member.getLoginId();
        this.nickname = member.getNickname();
        this.userImg = member.getMemberImage();
        this.role = member.getRole();
        this.thanks = thanks;
        this.sorry = sorry;
    }
}
