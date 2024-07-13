package ongjong.namanmoo.service;


import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private final AnswerRepository answerRepository;

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
    public List<Challenge> findChallengesByMemberId(Long challengeDate, Member member) throws Exception{      // 회원 아이디로 회원 조회
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

        // 그룹일 경우 대비해서 리스트로 (일반이면 1개, 그룹이면 2개)
        List <Challenge> challenges = findCurrentChallenges(member.getFamily().getFamilyId(), challengeDate);     //familyId를 통해 오늘의 챌린지 조회
        return challenges;
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



    // 오늘의 챌린지 반환 .그룹챌린지일 경우 같은 번호의 챌린지가 2개 이므로 리스트로 반환
    @Transactional(readOnly = true)
    public List<Challenge> findCurrentChallenges(Long familyId, Long challengeDate) {
        Integer number = findCurrentChallengeNum(familyId,challengeDate);
        if (number == null) {
            return null;
        }
        return challengeRepository.findByChallengeNum(number + luckyService.findStartChallengeNum(familyId));
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

    // 해당 Lucky에 대한 가장 많이 조회된 챌린지 찾기
    @Override
    public Challenge findMostViewedChallenge(Lucky lucky) throws Exception {
        Integer maxViews = 0;
        Integer mostViewedChallengeNum = null;

        // 모든 챌린지의 조회수를 총합하여 계산
        Map<Integer, Integer> totalViewsByChallengeNum = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : lucky.getChallengeViews().entrySet()) {
            Integer challengeNum = entry.getKey();
            Integer views = entry.getValue();
            totalViewsByChallengeNum.merge(challengeNum, views, Integer::sum);
            if (totalViewsByChallengeNum.get(challengeNum) > maxViews) {
                maxViews = totalViewsByChallengeNum.get(challengeNum);
                mostViewedChallengeNum = challengeNum;
            }
        }
        if (mostViewedChallengeNum != null) {
            Member member = memberService.findMemberByLoginId();
            for (Challenge challenge : challengeRepository.findAll()) {
                if (challenge.getChallengeNum().equals(mostViewedChallengeNum)) {
                    // Check if the challenge type is GROUP_CHILD or GROUP_PARENT
                    if (challenge.getChallengeType() == ChallengeType.GROUP_CHILD) {
                        // Retrieve the logged in member's role
                        String memberRole = member.getRole();
                        // Only return the challenge if the member's role is "아들" or "딸"
                        if (memberRole.equals("아들") || memberRole.equals("딸")) {
                            return challenge;
                        }
                    } else if (challenge.getChallengeType() == ChallengeType.GROUP_PARENT) {
                        // Retrieve the logged in member's role
                        String memberRole = member.getRole();
                        // Only return the challenge if the member's role is "엄마" or "아빠"
                        if (memberRole.equals("엄마") || memberRole.equals("아빠")) {
                            return challenge;
                        }
                    } else {
                        // For other challenge types, return the challenge directly
                        return challenge;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Challenge findFastestAnsweredChallenge(Lucky lucky) throws Exception {
        List<Challenge> challenges = findRunningChallenges(); // 해당 Lucky의 모든 챌린지 가져오기
        Challenge fastestChallenge = null;
        long shortestTime = Long.MAX_VALUE;

        for (Challenge challenge : challenges) {
            if (challenge.getChallengeType() == ChallengeType.GROUP_CHILD || challenge.getChallengeType() == ChallengeType.GROUP_PARENT) { // GROUP 챌린지일 때
                // 같은 challengeNum을 가지는 모든 챌린지 조회
                List<Challenge> relatedChallenges = challengeRepository.findByChallengeNum(challenge.getChallengeNum());
                log.info("그룹 챌린지 사이즈 " + relatedChallenges.size());

                int checkSize = 0;
                for (Challenge groupChallenge : relatedChallenges) {
                    List<Answer> groupAnswers = answerRepository.findByChallenge(groupChallenge);
                    checkSize += groupAnswers.size();
                }
                if (checkSize != lucky.getFamily().getMembers().size()) {
                    continue;
                }

                // 각 챌린지에 대해 답변 확인
                for (Challenge relatedChallenge : relatedChallenges) {
                    List<Answer> answers = answerRepository.findByChallenge(relatedChallenge);
                    log.info("Challenge ID: " + relatedChallenge.getChallengeId() + ", Answer count: " + answers.size());

                    long timeToAnswer = calculateFastestResponseTime(lucky, relatedChallenge); // 챌린지별 가장 늦은 응답 시간 계산
                    log.info("Challenge ID: " + relatedChallenge.getChallengeId() + ", Time to answer: " + timeToAnswer);
                    if (timeToAnswer < shortestTime) {
                        shortestTime = timeToAnswer;
                        fastestChallenge = relatedChallenge;
                    }
                }
            } else { // 그룹이 아닌 모든 챌린지
                // 해당 챌린지에 대한 모든 답변 가져오기
                List<Answer> answers = answerRepository.findByChallenge(challenge);
                log.info("Challenge ID: " + challenge.getChallengeId() + ", Answer count: " + answers.size());

                // 모든 가족 구성원이 답변했는지 확인
                if (answers.size() == lucky.getFamily().getMembers().size()) {
                    long timeToAnswer = calculateFastestResponseTime(lucky, challenge); // 챌린지별 가장 늦은 응답 시간 계산
                    log.info("Challenge ID: " + challenge.getChallengeId() + ", Time to answer: " + timeToAnswer);
                    if (timeToAnswer < shortestTime) {
                        shortestTime = timeToAnswer;
                        fastestChallenge = challenge;
                    }
                }
            }
        }
        log.info("Fastest Challenge ID: " + (fastestChallenge != null ? fastestChallenge.getChallengeId() : "None"));
        return fastestChallenge;
    }

    @Override
    public long calculateFastestResponseTime(Lucky lucky, Challenge challenge) throws Exception {
        long fastestTime = Long.MIN_VALUE; // 해당 챌린지의 가장 늦은 응답시간을 저장할 변수
        List<Answer> answers = answerRepository.findByChallenge(challenge);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        for (Member member : lucky.getFamily().getMembers()) {
            long latestResponseTimeForMember = Long.MIN_VALUE; // 가족 구성원의 가장 늦은 응답시간을 저장할 변수

            for (Answer answer : answers) {
                if (answer.getMember().equals(member)) {
                    try {
                        String modifiedDate = answer.getModifiedDate();

                        if (modifiedDate == null) {
                            log.warn("답변 ID: " + answer.getAnswerId() + "에서 Null 타임스탬프를 찾았습니다.");
                            continue;
                        }

                        Date modifiedTime = timeFormat.parse(modifiedDate.split(" ")[1]); // 시간 부분만 파싱
                        long responseTime = modifiedTime.getTime();

                        log.info("회원 ID: " + member.getMemberId() + ", 응답 시간: " + responseTime);

                        if (responseTime > latestResponseTimeForMember) {
                            latestResponseTimeForMember = responseTime;
                        }
                    } catch (ParseException e) {
                        log.error("답변 ID: " + answer.getAnswerId() + "의 날짜 파싱 에러", e);
                    }
                }
            }

            if (latestResponseTimeForMember != Long.MIN_VALUE && latestResponseTimeForMember > fastestTime) {
                fastestTime = latestResponseTimeForMember;
            }
        }

        if (fastestTime == Long.MIN_VALUE) {
            return Long.MAX_VALUE;
        }

        long totalSeconds = fastestTime / 1000;
        long totalMinutes = totalSeconds / 60;
        long totalHours = totalMinutes / 60;
        long days = totalHours / 24;
        long seconds = totalSeconds % 60;
        long minutes = totalMinutes % 60;
        long hours = totalHours % 24;
        log.info("가장 늦은 응답 시간: " + days + "일 " + hours + "시간 " + minutes + "분 " + seconds + "초");
        return fastestTime;
    }

}
