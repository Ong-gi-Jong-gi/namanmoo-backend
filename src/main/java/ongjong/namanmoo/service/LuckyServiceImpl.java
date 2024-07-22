package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LuckyServiceImpl implements LuckyService {

    private final LuckyRepository luckyRepository;
    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final AnswerRepository answerRepository;
    private final ChallengeRepository challengeRepository;

    // 캐릭터 생성
    @Override
    public boolean createLucky(Long familyId, Long challengeDate) throws Exception {
        Optional<Family> familyOptional = familyRepository.findById(familyId);

        // Family가 존재하지 않으면 false 반환
        if (familyOptional.isEmpty()) {
            throw new Exception("Family not found");
        }

        List<Lucky> luckyList = luckyRepository.findByFamilyFamilyId(familyId);
        // 모든 lucky의 running이 false인지 확인
        boolean allNotRunning = luckyList.stream().noneMatch(Lucky::isRunning);
        log.info("allNotRunning: " + allNotRunning);
        if (allNotRunning) {
            Family family = familyOptional.get();
            Lucky lucky = new Lucky();
            lucky.setFamily(family);
            lucky.setStatus(0);
            DateUtil dateUtil = DateUtil.getInstance();
            String currentDateStr = dateUtil.timestampToString(challengeDate);
            lucky.setChallengeStartDate(currentDateStr); // 문자열 형식으로 날짜 저장
            lucky.setRunning(true);       // 현재 진행하고있는 challenge에 따라 current challenge가 바뀌어야함 // 챌린지를 다시 시작할 경우 1이아닌 31이 될 수도 있어야함
            luckyRepository.save(lucky);
            return true;
        } else {
            throw new RuntimeException("Lucky already exists and is running");
        }
    }

    // 사용자의 챌린지 참여여부 확인하여 행운이 상태 반환
    @Override
    @Transactional(readOnly = true)
    public LuckyStatusDto getLuckyStatus(String challengeDate) throws IllegalArgumentException {
        String loginId = SecurityUtil.getLoginLoginId();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("No member found for login id: " + loginId));

        Family family = member.getFamily();
        Lucky lucky = luckyRepository.findByFamilyFamilyIdAndRunningTrue(family.getFamilyId())
                .orElseThrow(() -> new IllegalArgumentException("No active lucky for family id: " + family.getFamilyId()));

        // 타임스탬프를 yyyy.MM.dd 형식의 문자열로 변환
        long timestamp = Long.parseLong(challengeDate);
        Instant instant = Instant.ofEpochMilli(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneId.systemDefault());
        String createDate = formatter.format(instant);

        Answer answer = answerRepository.findByMemberAndCreateDate(member, createDate)
                .orElseThrow(() -> new RuntimeException("해당 멤버가 작성한 답변을 찾을 수 없습니다."));

        boolean isBubble = answer.isBubbleVisible();
        Integer luckyStatus = calculateLuckyStatus(lucky,createDate, answer.getAnswerContent());
//        Integer luckyStatus = calculateLuckyStatus(lucky);
        String luckyId = lucky.getLuckyId().toString();

        return new LuckyStatusDto(luckyStatus, isBubble, luckyId);
    }

    // 행운이 상태 계산
    @Override
    public Integer calculateLuckyStatus(Lucky lucky) {
        Integer familyContribution = lucky.getStatus(); // 가족의 총 챌린지 참여 횟수
        Family family = lucky.getFamily(); // 가족 정보 추출
        int maxFamilySize = family.getMaxFamilySize(); // 가족 최대 인원 수 추출
        int challengeDays = lucky.getLifetime().getDays(); // 챌린지 주기 추출

        // 비율 계산을 위한 분모 계산 (가족 최대 인원 수 x 챌린지 주기)
        int denominator = maxFamilySize * challengeDays;

        // 비율 계산
        double percentage = (double) familyContribution / denominator * 100;

        // 행운이 상태 결정
        if (percentage >= 75) { // 75% (30일 주기일 때 90개)
            return 3; // 행목
        } else if (percentage >= 25) { // 25% (30일 주기일 때 30개)
            return 2; // 행운
        } else {
            return 1; // 새싹
        }
    }

    // 시연용 행운이
    @Override
    public Integer calculateLuckyStatus(Lucky lucky, String answerCreateDate, String answerContent) {
        String luckyStartDate = lucky.getChallengeStartDate();
        Long betweenLuckyAnswer = DateUtil.getInstance().getDateDifference(luckyStartDate, answerCreateDate);
        if (betweenLuckyAnswer == 1) {
            return 1; // 1일 and (미참여 or 참여) : 새싹
        } else if (betweenLuckyAnswer == 15 && answerContent == null) {
            return 1; // 15일 and 미참여 : 새싹
        } else if (betweenLuckyAnswer == 15) {
            return 2; // 15일 and 참여 : 행운이
        } else if (betweenLuckyAnswer == 30 && answerContent == null) {
            return 2; // 30일 and 미참여 : 행운이
        } else {
            return 3; // 나머지 엑스텀프
        }
    }

    // 행운이 리스트 조회 ( RECAP list )
    @Override
    @Transactional(readOnly = true)
    public List<LuckyListDto> getLuckyListStatus() {
        // 현재 로그인한 사용자의 로그인 ID 가져오기
        String loginId = SecurityUtil.getLoginLoginId();

        // 로그인 ID로 회원 정보 조회
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("No member found for login id: " + loginId));

        // 회원의 가족 ID로 행운 리스트 조회
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(member.getFamily().getFamilyId());

        // 행운 리스트를 LuckyListDto로 매핑하여 반환
        return luckies.stream().map(lucky -> {
            // 도전 시작 날짜를 timestamp로 변환
            Long startDateTimestamp = DateUtil.getInstance().stringToTimestamp(lucky.getChallengeStartDate(), DateUtil.FORMAT_4);

            // 도전 시작 날짜에 도전 기간을 더하여 종료 날짜 timestamp 계산
            Long endDateTimestamp = DateUtil.getInstance().stringToTimestamp(
                    DateUtil.getInstance().addDaysToStringDate(
                            DateUtil.getInstance().timestampToString(startDateTimestamp), lucky.getLifetime().getDays()), DateUtil.FORMAT_4);

            // 행운 상태 계산
            Integer luckyStatus = calculateLuckyStatus(lucky);

            // LuckyListDto 객체 생성하여 반환
            return new LuckyListDto(lucky.getLuckyId().toString(), startDateTimestamp, endDateTimestamp, luckyStatus, lucky.isRunning());
        }).collect(Collectors.toList());
    }

    // luckyId로 lucky 찾기
    @Override
    @Transactional(readOnly = true)
    public Lucky getLucky(Long luckyId) {
        return luckyRepository.findById(luckyId).get();
    }


    // 현재 진행중인 lucky id 조회
    @Override
    @Transactional(readOnly = true)
    public Lucky findCurrentLucky(Long familyId) {
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(familyId);
        for (Lucky lucky : luckies) {
            if (lucky.isRunning()) {
                return lucky; // 현재 진행되고있는 luckyid 반환
            }
        }
        return null;
    }

    // 시작해야 하는 challenge 넘버 찾기
    @Override
    @Transactional(readOnly = true)
    public Integer findStartChallengeNum(Long familyId) {
        // luckies를 순회화면서 lucky의 lifetime의 합을 구하여 반환
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(familyId);

        int totalDays = luckies.stream()
                .filter(lucky -> !lucky.isRunning())
                .mapToInt(lucky -> lucky.getLifetime().getDays())
                .sum();
        return totalDays;
    }

    // 현재 실행 중인 Lucky 객체의 lifetime (챌린지 길이) 가져오기
    @Override
    @Transactional(readOnly = true)
    public Integer findCurrentLuckyLifetime(Long familyId) {
        return luckyRepository.findByFamilyFamilyId(familyId).stream()
                .filter(Lucky::isRunning)
                .findFirst()
                .map(lucky -> lucky.getLifetime().getDays())
                .orElse(0);
    }

    // 챌린지 조회 시 조회수 증가 로직
    @Override
    @Transactional
    public void increaseChallengeViews(Long luckyId, Integer challengeNum) {
        Lucky lucky = luckyRepository.findById(luckyId)
                .orElseThrow(() -> new RuntimeException("Lucky not found with id: " + luckyId));

        Long familyId = lucky.getFamily().getFamilyId();
        int startChallengeNum = luckyRepository.findByFamilyFamilyId(familyId).stream()
                .filter(l -> !l.isRunning())
                .mapToInt(l -> l.getLifetime().getDays())
                .sum();
        int endChallengeNum = startChallengeNum + lucky.getLifetime().getDays();

        // 주어진 챌린지 번호가 해당 Lucky의 범위 내에 있는지 확인
        if (challengeNum < startChallengeNum || challengeNum > endChallengeNum) {
            throw new IllegalArgumentException(
                    "Challenge number " + challengeNum + " is out of the current Lucky's range ("
                            + startChallengeNum + " - " + endChallengeNum + ").");
        }

        // 조회수 증가
        lucky.getChallengeViews().merge(challengeNum, 1, Integer::sum);

        luckyRepository.save(lucky); // 변경된 Lucky 엔티티 저장
    }

    // 챌린지가 종료되었을 경우 running -> false로 저장
    @Override
    public void luckyDeadOrAlive(String challengeDate) throws Exception {
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()).orElseThrow(() -> new Exception("회원이 없습니다"));
        Long familyId = member.getFamily().getFamilyId();
        int luckyLifetime = findCurrentLuckyLifetime(familyId);
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(familyId);

        for (Lucky lucky : luckies) {
            if (lucky.isRunning()) {
                DateUtil dateUtil = DateUtil.getInstance();
                // 현재 진행되어야할 challengenum를 반환
                int currentChallengeNumber = Math.toIntExact(dateUtil.getDateDifference(lucky.getChallengeStartDate(), dateUtil.timestampToString(Long.valueOf(challengeDate))));
                if (luckyLifetime < currentChallengeNumber) {
                    lucky.setRunning(false);
                    luckyRepository.save(lucky);
                }
            }
        }
    }
    // 해당 challengeId에 맞는 lucky를 찾기
    @Override
    public Lucky findMatchingLucky(Long challengeId, Member member) throws Exception {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("주어진 challengeId에 해당하는 챌린지를 찾을 수 없습니다."));
        Answer answer = answerRepository.findByChallengeAndMember(challenge,member)
                .orElseThrow(() -> new RuntimeException("해당 멤버가 작성한 답변을 찾을 수 없습니다."));
        String answerCreateDate = answer.getCreateDate();

        List<Lucky> luckyList = luckyRepository.findByFamilyFamilyId(member.getFamily().getFamilyId());

        Long answerCreateDateTimeStamp = DateUtil.getInstance().stringToTimestamp(answerCreateDate,DateUtil.FORMAT_4);
        String challengeStartDate1 =DateUtil.getInstance().addDaysToStringDate(answerCreateDate,-15);
        Long challengeStartDateTimeStamp1 = DateUtil.getInstance().stringToTimestamp(challengeStartDate1,DateUtil.FORMAT_4);
        for (Lucky lucky:luckyList){
            String luckyStartDate = lucky.getChallengeStartDate();
            Long luckyStartDateTimeStamp = DateUtil.getInstance().stringToTimestamp(luckyStartDate,DateUtil.FORMAT_4);
            if (luckyStartDateTimeStamp >= challengeStartDateTimeStamp1 && luckyStartDateTimeStamp <= answerCreateDateTimeStamp){
                return lucky;
            }
        }

        String challengeStartDate2 =DateUtil.getInstance().addDaysToStringDate(answerCreateDate,-30);
        Long challengeStartDateTimeStamp2 = DateUtil.getInstance().stringToTimestamp(challengeStartDate2,DateUtil.FORMAT_4);
        for (Lucky lucky:luckyList){
            String luckyStartDate = lucky.getChallengeStartDate();
            Long luckyStartDateTimeStamp = DateUtil.getInstance().stringToTimestamp(luckyStartDate,DateUtil.FORMAT_4);
            if (luckyStartDateTimeStamp >= challengeStartDateTimeStamp2 && luckyStartDateTimeStamp <= answerCreateDateTimeStamp){
                return lucky;
            }
        }
        return null;
    }
}
