package ongjong.namanmoo.dto.member;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Member;

@Data
@RequiredArgsConstructor
public class MemberInfoDto {

//    private final String loginId;
    private final String name;
    private final String nickName;
    private final String role;
    private final String memberImage;


    public MemberInfoDto(Member member) {
//        this.loginId = member.getLoginId();
        this.name = member.getName();
        this.nickName = member.getNickName();
        this.role = member.getRole();
        this.memberImage = member.getMemberImage();

    }
}
