package ongjong.namanmoo.dto.family;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FamilyMemberDto {
    private String userId;
    private String name;
    private String nickname;
    private String role;
    private String userImg;

}
