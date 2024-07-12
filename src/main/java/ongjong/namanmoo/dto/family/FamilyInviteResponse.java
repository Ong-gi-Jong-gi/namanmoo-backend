package ongjong.namanmoo.dto.family;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FamilyInviteResponse {
    private String familyName;
    private String familyId;
    private List<FamilyMemberDto> members;
}
