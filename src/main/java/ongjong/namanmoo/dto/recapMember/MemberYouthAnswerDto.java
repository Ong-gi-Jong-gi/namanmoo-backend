package ongjong.namanmoo.dto.recapMember;

import lombok.Data;
@Data
public class MemberYouthAnswerDto {
    private String userImg;
    private String photo;
    private String text;

    public MemberYouthAnswerDto(String userImg, String photo, String text){
        this.userImg = userImg;
        this.photo = photo;
        this.text = text;
    }
}
