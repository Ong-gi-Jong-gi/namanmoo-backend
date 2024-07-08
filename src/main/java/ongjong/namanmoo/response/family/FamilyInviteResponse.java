package ongjong.namanmoo.response.family;

import lombok.AllArgsConstructor;
import lombok.Data;
import ongjong.namanmoo.dto.family.FamilyMemberDto;

import java.util.List;

@Data
@AllArgsConstructor
public class FamilyInviteResponse {
    private String familyName;
    private String familyId;
    private List<FamilyMemberDto> members;
}
