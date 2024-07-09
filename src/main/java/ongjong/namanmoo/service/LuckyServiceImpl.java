package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.global.security.util.DateUtil;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.AnswerRepository;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LuckyServiceImpl implements LuckyService{
    private final LuckyRepository luckyRepository;
    private final MemberRepository memberRepository;
    private final AnswerService answerService;
    private final FamilyRepository familyRepository;


    public boolean createLucky(Long familyId, Long challengeDate){     // 캐릭터 생성
        Optional<Family> familyOptional = familyRepository.findById(familyId);

        if (familyOptional.isEmpty()) {
            return false; // Family가 존재하지 않으면 false 반환
        }

        List<Lucky> luckyList = luckyRepository.findByFamilyFamilyId(familyId);
        boolean allNotRunning = luckyList.stream().noneMatch(Lucky::isRunning); // 모든 lucky의 running이 false인지 확인
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


        boolean isBubble = answerService.checkUserResponse(member, createDate);
        Integer luckyStatus = calculateLuckyStatus(lucky);

        return new LuckyStatusDto(luckyStatus, isBubble);
    }



    // 행운이 상태 계산
    @Override
    public Integer calculateLuckyStatus(Lucky lucky) {
        Integer familyContribution = lucky.getStatus(); // 가족의 총 챌린지 참여 횟수

        // 비율 계산
        double percentage = (double) familyContribution / 120 * 100;

        // 행운이 상태 결정
        if (percentage >= 75) { // 90개
            return 3; // 행목
        } else if (percentage >= 25) { // 40개
            return 2; // 행운
        } else {
            return 1; // 새싹
        }

    }

}
