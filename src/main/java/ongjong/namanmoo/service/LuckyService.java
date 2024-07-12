package ongjong.namanmoo.service;



import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface LuckyService {
    /*
    * 행운이 단계 조회
    * 캐릭터 말풍선 갱신
    */

    LuckyStatusDto getLuckyStatus(String createDate);

    boolean createLucky(Long familyId, Long challengeDate);

    Integer calculateLuckyStatus(Lucky lucky);

    Lucky getLucky(Long luckyId);

    List<LuckyListDto> getLuckyListStatus();

    // 현재 진행중인 lucky id 조회
    Lucky findCurrentLucky(Long familyId);

    // 시작해야 하는 challenge 넘버 찾기
    Integer findStartChallengeNum(Long familyId);

    // 현재 실행 중인 Lucky 객체의 lifetime (챌린지 길이) 가져오기
    Integer findCurrentLuckyLifetime(Long familyId);
}
