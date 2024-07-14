package ongjong.namanmoo.dto.recap;

import lombok.Data;

import java.util.List;

@Data
public class MemberFacetimeDto {
    private List<String> facetimeAnswerList;

    public MemberFacetimeDto(List<String> facetimeAnswerList){
        this.facetimeAnswerList = facetimeAnswerList;
    }
}
