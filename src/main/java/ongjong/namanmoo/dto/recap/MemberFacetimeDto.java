package ongjong.namanmoo.dto.recap;

import lombok.Data;

import java.util.List;

@Data
public class MemberFacetimeDto {
    private long challengeDate;
    private List<String> video;

    public MemberFacetimeDto(long challengeDate, List<String> video){
        this.challengeDate = challengeDate;
        this.video = video;
    }
}