package ongjong.namanmoo.dto.family;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class FamilyMemberDto {
    private String name;
    private String nickname;
    private String role;
    private String userImg;
}
