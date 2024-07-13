package ongjong.namanmoo.dto.family;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateFamilyRequest {
    private int familySize;
    private String familyName;
    private String ownerRole;
}
