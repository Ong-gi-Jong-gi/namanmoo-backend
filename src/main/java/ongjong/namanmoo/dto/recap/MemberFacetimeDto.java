package ongjong.namanmoo.dto.recap;

import lombok.Data;

import java.util.List;

@Data
public class MemberFacetimeDto {
    private List<String> video;

    public MemberFacetimeDto(List<String> facetimeAnswerList){
        this.video = facetimeAnswerList;
    }
}
