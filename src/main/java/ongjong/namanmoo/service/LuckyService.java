package ongjong.namanmoo.service;



import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;

import java.util.List;


public interface LuckyService {
    /*
    * 행운이 단계 조회
    * 캐릭터 말풍선 갱신
    */

    // 사용자의 챌린지 참여여부 확인하여 행운이 상태 반환
    LuckyStatusDto getLuckyStatus(String createDate);

    // 캐릭터 생성
    boolean createLucky(Long familyId, Long challengeDate) throws Exception;

    // 행운이 상태 계산
    Integer calculateLuckyStatus(Lucky lucky);

    // 시연용 행운이 상태 계산
    Integer calculateLuckyStatus(Lucky lucky, String createDate, String answerContent);

    // luckyId로 lucky 찾기
    Lucky getLucky(Long luckyId);


    // 행운이 리스트 조회 ( RECAP list )
    List<LuckyListDto> getLuckyListStatus();

    // 현재 진행중인 lucky id 조회
    Lucky findCurrentLucky(Long familyId);

    // 시작해야 하는 challenge 넘버 찾기
    Integer findStartChallengeNum(Long familyId);

    // 현재 실행 중인 Lucky 객체의 lifetime (챌린지 길이) 가져오기
    Integer findCurrentLuckyLifetime(Long familyId);

    // 해당 Lucky에 해당하는 챌린지 조회수 늘리기
    void increaseChallengeViews(Long luckyId, Integer challengeNum);

    void luckyDeadOrAlive(String challengeDate) throws Exception;

    // 해당 challengeId에 맞는 lucky를 찾기
    Lucky findMatchingLucky(Long challengeId, Member member) throws Exception;
}
