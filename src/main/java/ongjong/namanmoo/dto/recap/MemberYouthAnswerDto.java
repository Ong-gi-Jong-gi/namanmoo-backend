package ongjong.namanmoo.dto.recap;

import lombok.Data;

@Data
public class MemberYouthAnswerDto implements MemberDto {
    private String userImg;
    private String photo;
    private String text;

    public MemberYouthAnswerDto(String userImg, String text, String photo){
        this.userImg = userImg;
        this.photo = photo;
        this.text = text;
    }
}