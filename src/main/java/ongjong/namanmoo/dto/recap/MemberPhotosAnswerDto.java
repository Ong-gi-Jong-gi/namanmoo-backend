package ongjong.namanmoo.dto.recap;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MemberPhotosAnswerDto {
    private String familyPhoto;
    private List<String> photoList;

    public MemberPhotosAnswerDto(String familyPhoto, List<String> photoList) {
        this.familyPhoto = familyPhoto;
        this.photoList = photoList;
    }
}
