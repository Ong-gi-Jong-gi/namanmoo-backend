package ongjong.namanmoo.service;


import lombok.RequiredArgsConstructor;

import ongjong.namanmoo.DateUtil;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeService {


    private final ChallengeRepository challengeRepository;
    private final LuckyRepository luckyRepository;
    private final MemberRepository memberRepository;



    // familyId를 통해 해당 날짜에 해당하는 오늘의 challenge 조회
    // 해당 가족 id를 가지고 있는 행운이 모두 조회
    // 행운이들 중 오늘의 챌린지 값이 30이 아닌 행운이의 오늘의 챌린지 값을 가져와야한다.
    //todo: 챌린지가 끝났을 때 오늘날짜와 챌린지시작날짜의 차이가 30일때 챌린지가 종료되어야한다. (challenge 테이블이 늘어날 수 있음을 고려 )

    @Transactional(readOnly = true)
    public List<Challenge> findCurrentChallenges(Long familyId, Long challengeDate) {       // 오늘의 챌린지 반환 .그룹챌린지일 경우 같은 번호의 챌린지가 2개 이므로 리스트로 반환
        Long number = findCurrentChallengeNum(familyId,challengeDate);
        if (number == null) {
            return null;
        }
        return challengeRepository.findByChallengeNum(number+findStartChallengeNum(familyId));
    }

    @Transactional(readOnly = true)
    public Long findCurrentChallengeNum(Long familyId, Long challengeDate) {       // 현재 진행중인 challenge 번호 조회
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(familyId);
        for (Lucky lucky : luckies) {
            if (lucky.isRunning()) {
                return getDateDifference(lucky.getChallengeStartDate(), getDateStirng(challengeDate)); // 현재 진행되어야할 challenge를 반환
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<Challenge> findChallenges(Long challengeDate) throws Exception{      // 현재 진행하고있는 행운이의 챌린지 리스트 가져오기
        Member member = findMemberByLoginId();  // 로그인한 member
        Family family = member.getFamily();

        Long number = findCurrentChallengeNum(family.getFamilyId(),challengeDate);      // 진행하는 challenge 번호
        if (number == null) {
            return null;
        }

        List<Challenge> challengeList  = challengeRepository.findByChallengeNumBetween(findStartChallengeNum(family.getId()), number);

        // 멤버 역할에 맞지 않는 challenge는 리스트에서 제외
        Iterator<Challenge> iterator = challengeList.iterator();        // iterator를 사용 -> challengelist를 순회하면서 조건에 맞지 않는 챌린지 제거
        while (iterator.hasNext()){
            Challenge challenge = iterator.next();
            if(challenge.getChallengeType() == ChallengeType.GROUP_PARENT){
                if (member.getRole().equals("아들") || member.getRole().equals("딸")){         //TODO: 현재 문자열 비교를 하고 있는데 ROLE을 enum으로 바꿔서 비교하는게 유지보수성이 좋다.
                    iterator.remove();
                }
            }
            else if (challenge.getChallengeType() == ChallengeType.GROUP_CHILD){
                if (member.getRole().equals("아빠") || member.getRole().equals("엄마")){
                    iterator.remove();
                }
            }
        }
        return challengeList;
    }

    @Transactional(readOnly = true)
    public Challenge findChallengeById(Long id){        // challenge id로 challenge 찾기
        return challengeRepository.findById(id).get();
    }

    @Transactional(readOnly = true)
    public Member findMemberByLoginId() throws Exception{      // 회원 아이디로 회원 조회
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()).orElseThrow(() -> new Exception("회원이 없습니다"));
        return member;
    }

    @Transactional(readOnly = true)     // 회원 아이디로 오늘의 챌린지 조회
    public List<Challenge> findChallengeByMemberId(Long challengeDate) throws Exception{      // 회원 아이디로 회원 조회
        Member member = findMemberByLoginId();  // 로그인한 member
        Family family = member.getFamily();

        int currentFamilySize = memberRepository.countByFamilyId(family.getFamilyId());
        if (currentFamilySize != family.getMaxFamilySize()) {
            return null;        // 현재 가족의수 가 max가족의 수와 같지 않을 겨우 오늘의 챌린지 조회 실패 -> null반환
        }

        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(family.getFamilyId());
        if (luckies.isEmpty()) {
            return null; // luckies 리스트가 비어있을 경우 null 반환
        }

        boolean validLuckyExists = luckies.stream()
                .anyMatch(Lucky::isRunning);
        if (!validLuckyExists) {
            return null;        // 진행중인 챌린지 , lucky가 없을 경우
        }

        List <Challenge> challenges = findCurrentChallenges(member.getFamily().getFamilyId(), challengeDate);     //familyId를 통해 오늘의 챌린지 조회
        return challenges;
    }

    @Transactional(readOnly = true)
    public Challenge findCurrentChallenge(List<Challenge> challenges) throws Exception{     // 오늘의 챌린지 조회
        if (challenges.isEmpty()) {
            return null;        // 오늘의 챌린지리스트가 비어있는 경우 null 리턴
        }
        if(challenges.size() == 1){
            return challenges.get(0);       // 오늘의 챌린지리스트 사이즈가 1이라면 첫번째 챌린지 반환
        }
        else if(challenges.size() == 2){    // 오늘의 챌린지리스트 사이즈가 2일 경우
            Challenge challenge1 = challenges.get(0);
            Challenge challenge2 = challenges.get(1);
            Member member = findMemberByLoginId();  //로그인한 member
            if (challenge1.getChallengeType() == ChallengeType.GROUP_PARENT){
                if (member.getRole().equals("아빠") || member.getRole().equals("엄마")){
                    return challenge1;
                }
                else{
                    return challenge2;
                }
            }
            else if (challenge1.getChallengeType() == ChallengeType.GROUP_CHILD){
                if (member.getRole().equals("아들") || member.getRole().equals("딸")){
                    return challenge1;
                }
                else{
                    return challenge2;
                }
            }
        }
        return null;
    }

    @Transactional(readOnly = true)     // 현재 날짜와 챌린지 시작 날짜를 비교하여 몇번째 챌린지를 진행중인지 반환
    public Long findCurrentNum(Long challengeDate) throws Exception{
        //현재 맴버 찾고 가족찾고 ,lucky찾아서 lucky의 challenge start date구해서 challengedate 빼기
        Member member = findMemberByLoginId();  // 로그인한 member
        Family family = member.getFamily();
        return findCurrentChallengeNum(family.getFamilyId(),challengeDate);
    }

    @Transactional(readOnly = true)
    public String getDateStirng(Long challengeDate) {       // timstamp형식을   "yyyy.MM.dd"형식의 문자열로 바꾸기
        DateUtil dateUtil = DateUtil.getInstance();
        return dateUtil.getDateStr(challengeDate, DateUtil.FORMAT_4);
    }

    @Transactional(readOnly = true)
    public Long getDateDifference(String dateStr1, String dateStr2) {               // 두 문자열로 들어오는날짜의 차이를 계산
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        // 문자열을 LocalDate로 변환
        LocalDate date1 = LocalDate.parse(dateStr1, formatter);
        LocalDate date2 = LocalDate.parse(dateStr2, formatter);

        // 두 날짜의 차이 계산
        return ChronoUnit.DAYS.between(date1, date2)+1;
    }

    @Transactional
    public Long findStartChallengeNum(Long familyId){     // 시작해야하는 challenge 넘버 찾기
        // lukies를 순회화면서 lucky의 boolean 타입인 running이 false인 lucky의 개수를 찾고
        // 찾은 개수 * 30 + 1부터 number까지의 challengenum 으로 challengeList를 구하는 걸로 변경

        List <Lucky> luckies = luckyRepository.findByFamilyId(familyId);
        long nonRunningCount = luckies.stream()
                .filter(lucky -> !lucky.isRunning())
                .count();

        return nonRunningCount * 30 ;
    }
}
