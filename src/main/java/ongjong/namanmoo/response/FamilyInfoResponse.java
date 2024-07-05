package ongjong.namanmoo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import ongjong.namanmoo.dto.family.FamilyMemberDto;

import java.util.List;

@Data
@AllArgsConstructor
public class FamilyInfoResponse {
    private List<FamilyMemberDto> members;
}
