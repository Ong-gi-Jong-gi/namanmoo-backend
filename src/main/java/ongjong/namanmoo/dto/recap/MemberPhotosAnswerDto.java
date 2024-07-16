package ongjong.namanmoo.dto.recap;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MemberPhotosAnswerDto {
    private String familyPhoto;
    private List<String> others;

    public MemberPhotosAnswerDto(String familyPhoto, List<String> photoList) {
        this.familyPhoto = familyPhoto;
        this.others = photoList;
    }
}
