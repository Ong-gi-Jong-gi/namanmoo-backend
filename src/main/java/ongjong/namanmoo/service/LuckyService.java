package ongjong.namanmoo.service;


import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.dto.lucky.OffBubbleDto;


public interface LuckyService {
    /*
    * 행운이 단계 조회
    * 캐릭터 말풍선 갱신
    */

    LuckyStatusDto getLuckyStatus(String createDate);

    boolean createLucky(Long familyId);

    Integer calculateLuckyStatus(Family Family);

}
