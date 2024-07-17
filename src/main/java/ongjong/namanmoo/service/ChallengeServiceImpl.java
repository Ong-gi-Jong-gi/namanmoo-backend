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
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private final AnswerRepository answerRepository;

    // familyId를 통해 해당 날짜에 해당하는 오늘의 challenge 조회
    // 해당 가족 id를 가지고 있는 행운이 모두 조회
    // 행운이들 중 오늘의 챌린지 값이 30이 아닌 행운이의 오늘의 챌린지 값을 가져와야한다.

    // 현재 진행하고 있는 행운이의 챌린지 리스트 가져오기
    @Override
    @Transactional(readOnly = true)
    public List<Challenge> findChallenges(Long challengeDate) throws Exception{
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()).orElseThrow(() -> new Exception("회원이 없습니다"));  // 로그인한 member
        Family family = member.getFamily();
        Optional<Lucky> lucky = luckyRepository.findByFamilyFamilyIdAndRunningTrue(family.getFamilyId());
        if (lucky.isEmpty()) {
            throw new Exception("행운이가 없습니다");
        }

        Integer number = findCurrentChallengeNum(family.getFamilyId(),challengeDate);      // 진행하는 challenge 번호
        if (number == null) {
            return null;
        }
        // TODO: 챌린지 리스트 다 보여주기 위해 일단은 최대 값인 lucky의 주기로 설정 -> 추후에 현재 날짜에 해당하는 챌린지 번호인 number로 변경 필요
        List<Challenge> challengeList = challengeRepository.findByChallengeNumBetween(luckyService.findStartChallengeNum(family.getFamilyId()), lucky.get().getLifetime().getDays());

        // 멤버 역할에 맞지 않는 challenge는 리스트에서 제외
        return groupChallengeExceptionRemove(challengeList,member);
    }

    // 멤버 역할에 맞지 않는 challenge는 리스트에서 제외
    @Override
    @Transactional(readOnly = true)
    public List<Challenge> groupChallengeExceptionRemove(List<Challenge> challengeList, Member member) throws Exception{
        Iterator<Challenge> iterator = challengeList.iterator();        // iterator를 사용 -> challengelist를 순회하면서 조건에 맞지 않는 챌린지 제거
        while (iterator.hasNext()){
            Challenge challenge = iterator.next();
            if(challenge.getChallengeType() == ChallengeType.GROUP_PARENT){
                if (member.getRole().equals("아들") || member.getRole().equals("딸")){
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
        boolean isDone = false;

        int currentFamilySize = memberRepository.countByFamilyId(family.getFamilyId());
        if (currentFamilySize != family.getMaxFamilySize()) {
            return new CurrentChallengeDto(isDone, null);  // 현재 가족의수 가 max가족의 수와 같지 않을 경우 오늘의 챌린지 조회 실패 -> null 반환
        }

        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(family.getFamilyId());
        if (luckies.isEmpty()) {
            return new CurrentChallengeDto(isDone, null); // luckies 리스트가 비어있을 경우 null 반환
        }

        boolean validLuckyExists = luckies.stream()
                .anyMatch(Lucky::isRunning);
        if (!validLuckyExists) {
            isDone = true; // 진행한 lucky의 챌린지를 모두 했을 경우
            return new CurrentChallengeDto(isDone, null); // 진행중인 챌린지, lucky가 없을 경우
        }

        return findCurrentChallenges(member.getFamily().getFamilyId(), challengeDate);
    }

    // 오늘의 챌린지 조회
    @Override
    @Transactional(readOnly = true)
    public Challenge findOneInCurrentChallenges(List<Challenge> challenges) throws Exception{
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()).orElseThrow(() -> new Exception("회원이 없습니다"));  //로그인한 member
        if (challenges.isEmpty()) {
            return null;        // 오늘의 챌린지리스트가 비어있는 경우 null 리턴
        }
        if(challenges.size() == 1){
            return challenges.get(0);       // 오늘의 챌린지리스트 사이즈가 1이라면 첫번째 챌린지 반환
        }
        else if(challenges.size() == 2) {    // 오늘의 챌린지리스트 사이즈가 2일 경우
            Challenge challenge1 = challenges.get(0);
            Challenge challenge2 = challenges.get(1);
            if (challenge1.getChallengeType() == ChallengeType.GROUP_PARENT) {
                if (member.getRole().equals("아빠") || member.getRole().equals("엄마")) {
                    return challenge1;
                } else {
                    return challenge2;
                }
            } else if (challenge1.getChallengeType() == ChallengeType.GROUP_CHILD) {
                if (member.getRole().equals("아들") || member.getRole().equals("딸")) {
                    return challenge1;
                } else {
                    return challenge2;
                }
            }
        }
        else if(challenges.size() == 4){
            List<Member> memberList = memberRepository.findByFamilyFamilyId(member.getFamily().getFamilyId());
            for(int i = 0; i < memberList.size(); i++){
                if(memberList.get(i) == member){
                    return challenges.get(i%4);
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
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()).orElseThrow(() -> new Exception("회원이 없습니다")); // 로그인한 member
        Family family = member.getFamily();
        return findCurrentChallengeNum(family.getFamilyId(), challengeDate);
    }

    // 현재 진행하고 있는 챌린지를 행운이의 챌린지 길이만큼 가져오기
    @Override
    @Transactional(readOnly = true)
    public List<Challenge> findRunningChallenges() throws Exception {
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()).orElseThrow(() -> new Exception("회원이 없습니다")); // 로그인한 member
        Family family = member.getFamily();

        int startChallengeNum = luckyService.findStartChallengeNum(family.getFamilyId());

        // Calculating the total number of days for the currently running Lucky
        int runningLuckyLifetime = luckyService.findCurrentLuckyLifetime(family.getFamilyId());

        return challengeRepository.findByChallengeNumBetween(startChallengeNum, startChallengeNum + runningLuckyLifetime);
    }

    // groupChallenge 조회를 위한 dto  (부모와 자식의 challenge 질문 구분하기)
    @Override
    @Transactional(readOnly = true)
    public GroupChallengeDto filterChallengesByMemberRole(Challenge challenge, Long timeStamp, boolean isComplete, List<Answer> answers) {
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

    // 오늘의 챌린지 반환 (그룹챌린지일 경우 같은 번호의 챌린지가 2개 이므로 리스트로 반환, 음성챌린지의 경우 4개)
    @Transactional(readOnly = true)
    public CurrentChallengeDto findCurrentChallenges(Long familyId, Long challengeDate) throws Exception {
        boolean isDone = false;
        Integer number = findCurrentChallengeNum(familyId, challengeDate);

        List<Challenge> challenges = challengeRepository.findByChallengeNum(number + luckyService.findStartChallengeNum(familyId));

        // 더 이상 챌린지를 찾을 수 없을 경우 // isDone이 false인데 challengeInfo가 null이라면 챌린지가 부족함을 의미
        if (challenges.isEmpty()) {
            return new CurrentChallengeDto(isDone, null);
        }

        Challenge challenge = findOneInCurrentChallenges(challenges);       // 그룹 질문 구분하기
        DateUtil dateUtil = DateUtil.getInstance();
        CurrentChallengeDto.ChallengeInfo challengeDto = new CurrentChallengeDto.ChallengeInfo(challenge, number, dateUtil.timestampToString(challengeDate));
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
                        // TODO: 음성챌린지 회원에 맞게 보여야함
//                    } else if (challenge.getChallengeType() == ChallengeType.VOICE1) {
//
//                    } else if (challenge.getChallengeType() == ChallengeType.VOICE2) {
//
//                    } else if (challenge.getChallengeType() == ChallengeType.VOICE3) {
//
//                    } else if (challenge.getChallengeType() == ChallengeType.VOICE4) {
//
                    } else {
                        // For other challenge types, return the challenge directly
                        return challenge;
                    }
                }
            }
        }
        return null;
    }

    // 모든 가족 구성원이 가장 빠르게 답한 챌린지를 반환
    @Override
    public Challenge findFastestAnsweredChallenge(Lucky lucky) throws Exception {
        Family family = lucky.getFamily();
        List<Challenge> challenges = findRunningChallenges(); // 해당 Lucky의 모든 챌린지 가져오기
        Challenge fastestChallenge = null;
        long shortestTime = Long.MAX_VALUE;

        for (Challenge challenge : challenges) {
            if (challenge.getChallengeType() == ChallengeType.GROUP_CHILD ||   // 답변 당 여러개의 챌린지ID를 가지는 질문들 (ex. GROUP, VOICE)
                    challenge.getChallengeType() == ChallengeType.GROUP_PARENT ||
                    challenge.getChallengeType() == ChallengeType.VOICE1 ||
                    challenge.getChallengeType() == ChallengeType.VOICE2 ||
                    challenge.getChallengeType() == ChallengeType.VOICE3 ||
                    challenge.getChallengeType() == ChallengeType.VOICE4) {
                // 같은 challengeNum을 가지는 모든 챌린지 조회
                List<Challenge> relatedChallenges = challengeRepository.findByChallengeNum(challenge.getChallengeNum());
                log.info("그룹 챌린지 사이즈 " + relatedChallenges.size());

                int checkSize = 0;
                for (Challenge relatedChallenge : relatedChallenges) {
                    List<Answer> relatedAnswers = answerRepository.findByChallengeAndMemberFamily(relatedChallenge, family);
                    checkSize += relatedAnswers.size();
                }
                if (checkSize != lucky.getFamily().getMembers().size()) {
                    continue;
                }

                // 각 챌린지에 대해 답변 확인
                for (Challenge relatedChallenge : relatedChallenges) {
                    List<Answer> answers = answerRepository.findByChallengeAndMemberFamily(relatedChallenge, family);
                    log.info("Challenge ID: " + relatedChallenge.getChallengeId() + ", Answer count: " + answers.size());

                    // 해당 챌린지에 대해 모든 가족 구성원이 답변 내용을 입력했는지 확인
                    boolean allMembersAnswered = true;
                    for (Answer answer : answers) {
                        if (answer.getAnswerContent() == null || answer.getAnswerContent().isEmpty()) {
                            allMembersAnswered = false;
                            break;
                        }
                    }

                    if (allMembersAnswered) {
                        long timeToAnswer = calculateLatestResponseTime(lucky, relatedChallenge); // 챌린지별 가장 늦은 응답 시간 계산
                        log.info("Challenge ID: " + relatedChallenge.getChallengeId() + ", Time to answer: " + timeToAnswer);
                        if (timeToAnswer < shortestTime) {
                            shortestTime = timeToAnswer;
                            fastestChallenge = relatedChallenge;
                        }
                    }
                }
            } else { // 답변 당 하나의 챌린지ID만 가지는 질문들 (ex. NORMAL, PHOTO, FACETIME)
                // 해당 챌린지에 대한 모든 답변 가져오기
                List<Answer> answers = answerRepository.findByChallengeAndMemberFamily(challenge, family);
                log.info("Challenge ID: " + challenge.getChallengeId() + ", Answer count: " + answers.size());

                // 해당 챌린지에 대해 모든 가족 구성원이 답변 내용을 입력했는지 확인
                boolean allMembersAnswered = true;
                for (Answer answer : answers) {
                    if (answer.getAnswerContent() == null || answer.getAnswerContent().isEmpty()) {
                        allMembersAnswered = false;
                        break;
                    }
                }

                if (allMembersAnswered) {
                    long timeToAnswer = calculateLatestResponseTime(lucky, challenge); // 챌린지별 가장 늦은 응답 시간 계산
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
    public long calculateLatestResponseTime(Lucky lucky, Challenge challenge) throws Exception {
        long latestTime = Long.MIN_VALUE; // 해당 챌린지의 가장 늦은 응답시간을 저장할 변수
        List<Answer> answers = answerRepository.findByChallenge(challenge);

        SimpleDateFormat format4 = new SimpleDateFormat(DateUtil.FORMAT_4);
        SimpleDateFormat format9 = new SimpleDateFormat(DateUtil.FORMAT_9);

        for (Member member : lucky.getFamily().getMembers()) {
            long latestResponseTimeForMember = Long.MIN_VALUE; // 가족 구성원의 가장 늦은 응답시간을 저장할 변수

            for (Answer answer : answers) {
                if (answer.getMember().equals(member)) {
                    try {
                        String createDate = answer.getCreateDate();
                        String modifiedDate = answer.getModifiedDate();

                        if (createDate == null || modifiedDate == null) {
                            log.warn("답변 ID: {}에서 Null 타임스탬프를 찾았습니다.", answer.getAnswerId());
                            continue;
                        }

                        Date createTime = format4.parse(createDate);
                        Date modifiedTime = format9.parse(modifiedDate);
                        log.info("답변 수정 시간: {}, 답변 생성 시간: {}", modifiedTime.getTime(), createTime.getTime());

                        long responseTime = Math.abs(modifiedTime.getTime() - createTime.getTime()); // 원래라면 수정 날짜가 답변이 열리는 날짜보다 나중이기 때문에 음수가 나올일이 없지만 올바른 시연을 위해 절대값 처리
                        log.info("회원 ID: {}, 응답 시간: {}", member.getMemberId(), responseTime);

                        if (responseTime > latestResponseTimeForMember) {
                            latestResponseTimeForMember = responseTime;
                        }
                    } catch (ParseException e) {
                        log.error("답변 ID: {}의 날짜 파싱 에러", answer.getAnswerId(), e);
                    }
                }
            }

            if (latestResponseTimeForMember > latestTime) {
                latestTime = latestResponseTimeForMember;
            }
        }

        if (latestTime == Long.MIN_VALUE) {
            return Long.MAX_VALUE;
        }
        long totalSeconds = latestTime / 1000;
        long totalMinutes = totalSeconds / 60;
        long totalHours = totalMinutes / 60;
        long days = totalHours / 24;
        long seconds = totalSeconds % 60;
        long minutes = totalMinutes % 60;
        long hours = totalHours % 24;
        log.info("가장 늦은 응답 시간: " + days + "일 " + hours + "시간 " + minutes + "분 " + seconds + "초");
        return latestTime;
    }

}
