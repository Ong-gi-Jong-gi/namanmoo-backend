package ongjong.namanmoo.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Member;

@Data
@RequiredArgsConstructor
public class MemberInfoDto {

//    private final String loginId;
    private final String name;
    private final String nickname;
    private final String role;
    private final String userImg;
    private final String code;


    public MemberInfoDto(Member member) {
//        this.loginId = member.getLoginId();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.role = member.getRole();
        this.userImg = (member.getMemberImage() != null) ? member.getMemberImage() : "";
        this.code = (member.getFamily() != null) ? member.getFamily().getInviteCode() : "";
    }
}
