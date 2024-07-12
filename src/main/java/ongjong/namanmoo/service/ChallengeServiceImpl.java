package ongjong.namanmoo.service;


import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.dto.answer.AnswerDto;
import ongjong.namanmoo.dto.challenge.CurrentChallengeDto;
import ongjong.namanmoo.dto.challenge.GroupChallengeDto;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {


    private final ChallengeRepository challengeRepository;
    private final LuckyRepository luckyRepository;
    private final LuckyService luckyService;
    private final MemberRepository memberRepository;
    private final MemberService memberService;


    // familyId를 통해 해당 날짜에 해당하는 오늘의 challenge 조회
    // 해당 가족 id를 가지고 있는 행운이 모두 조회
    // 행운이들 중 오늘의 챌린지 값이 30이 아닌 행운이의 오늘의 챌린지 값을 가져와야한다.
    //todo: 챌린지가 끝났을 때 오늘날짜와 챌린지시작날짜의 차이가 30일때 챌린지가 종료되어야한다. (challenge 테이블이 늘어날 수 있음을 고려 )

    // 현재 진행하고 있는 행운이의 챌린지 리스트 가져오기
    @Override
    @Transactional(readOnly = true)
    public List<Challenge> findChallenges(Long challengeDate) throws Exception{
        Member member = memberService.findMemberByLoginId();  // 로그인한 member
        Family family = member.getFamily();

        Integer number = findCurrentChallengeNum(family.getFamilyId(),challengeDate);      // 진행하는 challenge 번호
        if (number == null) {
            return null;
        }

        log.info("number: {}",number);
        List<Challenge> challengeList = challengeRepository.findByChallengeNumBetween(luckyService.findStartChallengeNum(family.getFamilyId()), number);

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

    // challenge id로 challenge 찾기
    @Override
    @Transactional(readOnly = true)
    public Challenge findChallengeById(Long id){
        return challengeRepository.findById(id).get();
    }

    // 회원 아이디로 오늘의 챌린지 조회
    @Override
    @Transactional(readOnly = true)
    public CurrentChallengeDto findChallengesByMemberId(Long challengeDate, Member member) throws Exception{      // 회원 아이디로 회원 조회
        Family family = member.getFamily();

        int currentFamilySize = memberRepository.countByFamilyId(family.getFamilyId());
        if (currentFamilySize != family.getMaxFamilySize()) {
            return null; // 현재 가족의수 가 max가족의 수와 같지 않을 경우 오늘의 챌린지 조회 실패 -> null 반환
        }

        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(family.getFamilyId());
        if (luckies.isEmpty()) {
            return null; // luckies 리스트가 비어있을 경우 null 반환
        }

        boolean validLuckyExists = luckies.stream()
                .anyMatch(Lucky::isRunning);
        if (!validLuckyExists) {
            return null; // 진행중인 챌린지, lucky가 없을 경우
        }

        return findCurrentChallenges(member.getFamily().getFamilyId(), challengeDate);
    }

    // 오늘의 챌린지 조회
    @Override
    @Transactional(readOnly = true)
    public Challenge findOneInCurrentChallenges(List<Challenge> challenges) throws Exception{
        if (challenges.isEmpty()) {
            return null;        // 오늘의 챌린지리스트가 비어있는 경우 null 리턴
        }
        if(challenges.size() == 1){
            return challenges.get(0);       // 오늘의 챌린지리스트 사이즈가 1이라면 첫번째 챌린지 반환
        }
        else if(challenges.size() == 2){    // 오늘의 챌린지리스트 사이즈가 2일 경우
            Challenge challenge1 = challenges.get(0);
            Challenge challenge2 = challenges.get(1);
            Member member = memberService.findMemberByLoginId();  //로그인한 member
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

    // 현재 날짜와 챌린지 시작 날짜를 비교하여 몇번째 챌린지를 진행중인지 반환
    @Override
    @Transactional(readOnly = true)
    public Integer findCurrentNum(Long challengeDate) throws Exception{
        //현재 맴버 찾고 가족찾고 ,lucky찾아서 lucky의 challenge start date구해서 challengedate 빼기
        Member member = memberService.findMemberByLoginId();  // 로그인한 member
        Family family = member.getFamily();
        return findCurrentChallengeNum(family.getFamilyId(), challengeDate);
    }

    // 현재 진행하고 있는 챌린지를 행운이의 챌린지 길이만큼 가져오기
    @Override
    @Transactional(readOnly = true)
    public List<Challenge> findRunningChallenges() throws Exception {
        Member member = memberService.findMemberByLoginId();  // 로그인한 member
        Family family = member.getFamily();

        int startChallengeNum = luckyService.findStartChallengeNum(family.getFamilyId());

        // Calculating the total number of days for the currently running Lucky
        int runningLuckyLifetime = luckyService.findCurrentLuckyLifetime(family.getFamilyId());

        return challengeRepository.findByChallengeNumBetween(startChallengeNum, startChallengeNum + runningLuckyLifetime);
    }

    // groupChallenge 조회를 위한 dto  (부모와 자식의 challenge 질문 구분하기)
    @Override
    @Transactional(readOnly = true)
    public GroupChallengeDto createGroupChallenge(Challenge challenge, Long timeStamp, boolean isComplete, List<Answer> answers) {
        List<Answer> parentAnswerList = new ArrayList<>();
        List<Answer> childAnswerList = new ArrayList<>();

        for (Answer answer : answers) {
            if (answer.getChallenge().getChallengeType() == ChallengeType.GROUP_PARENT) {
                parentAnswerList.add(answer);
            } else {
                childAnswerList.add(answer);
            }
        }
        List<AnswerDto> parentAnswerDtoList = parentAnswerList.stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
        GroupChallengeDto.NewChallengeDto parentChallenge = parentAnswerList.isEmpty() ?
                new GroupChallengeDto.NewChallengeDto("No Parent Challenge", new ArrayList<>()) :
                new GroupChallengeDto.NewChallengeDto(parentAnswerList.get(0).getChallenge().getChallengeTitle(), parentAnswerDtoList);

        List<AnswerDto> childAnswerDtoList = childAnswerList.stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
        GroupChallengeDto.NewChallengeDto childrenChallenge = childAnswerList.isEmpty() ?
                new GroupChallengeDto.NewChallengeDto("No Children Challenge", new ArrayList<>()) :
                new GroupChallengeDto.NewChallengeDto(childAnswerList.get(0).getChallenge().getChallengeTitle(), childAnswerDtoList);

        return new GroupChallengeDto(challenge.getChallengeNum().toString(), timeStamp, isComplete, parentChallenge, childrenChallenge);
    }

    // 오늘의 챌린지 반환 .그룹챌린지일 경우 같은 번호의 챌린지가 2개 이므로 리스트로 반환
    @Transactional(readOnly = true)
    public CurrentChallengeDto findCurrentChallenges(Long familyId, Long challengeDate) throws Exception {
        boolean isDone = false;
        Integer number = findCurrentChallengeNum(familyId, challengeDate);
        int runningLuckyLifetime = luckyService.findCurrentLuckyLifetime(familyId);

        if (number == null) {
            return new CurrentChallengeDto(isDone, null);
        }

        if (number > runningLuckyLifetime) {
            isDone = true; // 진행한 lucky의 챌린지를 모두 했을 경우
        }

        List<Challenge> challenges = challengeRepository.findByChallengeNum(number + luckyService.findStartChallengeNum(familyId));

        if (challenges.isEmpty()) {
            return new CurrentChallengeDto(isDone, null);
        }

        Challenge challenge = findOneInCurrentChallenges(challenges);       // 그룹 질문 구분하기
        DateUtil dateUtil = DateUtil.getInstance();
        CurrentChallengeDto.ChallengeDto challengeDto = new CurrentChallengeDto.ChallengeDto(challenge, number, dateUtil.timestampToString(challengeDate));
        return new CurrentChallengeDto(isDone, challengeDto);
    }

    // 현재 진행중인 challenge 번호 조회
    @Transactional(readOnly = true)
    public Integer findCurrentChallengeNum(Long familyId, Long challengeDate) {
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(familyId);
        for (Lucky lucky : luckies) {
            if (lucky.isRunning()) {
                DateUtil dateUtil = DateUtil.getInstance();
                return Math.toIntExact(dateUtil.getDateDifference(lucky.getChallengeStartDate(), dateUtil.timestampToString(challengeDate))); // 현재 진행되어야할 challenge를 반환
            }
        }
        return null;
    }
}
