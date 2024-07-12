package ongjong.namanmoo.dto.family;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FamilyInfoResponse {
    private List<FamilyMemberDto> members;

}
