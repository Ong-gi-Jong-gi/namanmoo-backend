package ongjong.namanmoo.dto.family;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinFamilyRequest {
    private Long familyId;
    private String role;
}
