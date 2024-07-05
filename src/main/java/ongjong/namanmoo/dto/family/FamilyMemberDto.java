package ongjong.namanmoo.dto.family;

import lombok.Data;
import lombok.Getter;


@Data
public class FamilyMemberDto {
    private String userId;
    private String name;
    private String nickname;
    private String role;
    private String userImg;
}
