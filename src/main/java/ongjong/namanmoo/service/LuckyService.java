package ongjong.namanmoo.service;



import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;

import java.util.List;


public interface LuckyService {
    /*
    * 행운이 단계 조회
    * 캐릭터 말풍선 갱신
    */

    LuckyStatusDto getLuckyStatus(String createDate);

    boolean createLucky(Long familyId, Long challengeDate);

    Integer calculateLuckyStatus(Lucky lucky);

    List<LuckyListDto> getLuckyListStatus();

    void increaseChallengeViews(Long luckyId, Integer challengeNum);

    Lucky findCurrentLucky(Long familyId);
}
