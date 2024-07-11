package ongjong.namanmoo.dto.recapMember;

import lombok.Data;

import java.util.List;

@Data
public class MemberRankingListDto {
    private Integer totalCount;
    private Integer luckyStatus;
    private List<MemberAndCountDto> ranking;

    public MemberRankingListDto(Integer totalCount, Integer luckyStatus, List<MemberAndCountDto> ranking) {
        this.totalCount = totalCount;
        this.luckyStatus = luckyStatus;
        this.ranking = ranking;
    }

}
