package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.AnswerRepository;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.MemberRepository;
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
public class LuckyServiceImpl implements LuckyService{

    private final LuckyRepository luckyRepository;
    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final AnswerRepository answerRepository;

    // 캐릭터 생성
    @Override
    public boolean createLucky(Long familyId, Long challengeDate){
        Optional<Family> familyOptional = familyRepository.findById(familyId);

        // Family가 존재하지 않으면 false 반환
        if (familyOptional.isEmpty()) {
            return false;
        }

        List<Lucky> luckyList = luckyRepository.findByFamilyFamilyId(familyId);
        // 모든 lucky의 running이 false인지 확인
        boolean allNotRunning = luckyList.stream().noneMatch(Lucky::isRunning);
       if (allNotRunning) {
            Family family = familyOptional.get();
            Lucky lucky = new Lucky();
            lucky.setFamily(family);
            lucky.setStatus(1);
            DateUtil dateUtil = DateUtil.getInstance();
            String currentDateStr = dateUtil.timestampToString(challengeDate);
            lucky.setChallengeStartDate(currentDateStr); // 문자열 형식으로 날짜 저장
            lucky.setRunning(true);       // 현재 진행하고있는 challenge에 따라 current challenge가 바뀌어야함 // 챌린지를 다시 시작할 경우 1이아닌 31이 될 수도 있어야함
            luckyRepository.save(lucky);
            return true;
        } else {
            return false;
        }
    }

    // 사용자의 챌린지 참여여부 확인하여 행운이 상태 반환
    @Override
    @Transactional(readOnly = true)
    public LuckyStatusDto getLuckyStatus(String challengeDate) throws IllegalArgumentException{
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
        log.info("createDate: {}", createDate);


        boolean isBubble = answerRepository.existsByMemberAndCreateDateAndAnswerContentIsNotNull(member, createDate);
        Integer luckyStatus = calculateLuckyStatus(lucky);

        return new LuckyStatusDto(luckyStatus, isBubble);
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
        } else if (percentage >= 25) { // 25% (30일 주기일 때 40개)
            return 2; // 행운
        } else {
            return 1; // 새싹
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
            return new LuckyListDto(lucky.getLuckyId().toString(), startDateTimestamp, endDateTimestamp, luckyStatus);
        }).collect(Collectors.toList());
    }

    // luckyId로 lucky 찾기
    @Override
    @Transactional(readOnly = true)
    public Lucky getLucky(Long luckyId){
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
    public Integer findStartChallengeNum(Long familyId){
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
}
