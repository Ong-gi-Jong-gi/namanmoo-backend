package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
@Transactional
public class LuckyServiceImpl implements LuckyService{
    private final LuckyRepository luckyRepository;
    private final MemberRepository memberRepository;
    private final AnswerService answerService;

    // 사용자의 챌린지 참여여부 확인하여 행운이 상태 반환
    @Override
    @Transactional(readOnly = true)
    public LuckyStatusDto getLuckyStatus(Timestamp createDate) throws IllegalArgumentException{
        String loginId = SecurityUtil.getLoginLoginId();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("No member found for login id: " + loginId));

        Family family = member.getFamily();
        Lucky lucky = luckyRepository.findByFamilyFamilyIdAndRunningTrue(family.getFamilyId())
                .orElseThrow(() -> new IllegalArgumentException("No active lucky for family id: " + family.getFamilyId()));

        boolean isBubble = answerService.checkUserResponse(member, createDate);
        Integer luckyStatus = calculateLuckyStatus(lucky.getFamily());

        return new LuckyStatusDto(luckyStatus, isBubble);
    }



    // 행운이 상태 계산
    @Override
    public Integer calculateLuckyStatus(Family family) {
        Integer familyContribution = family.getChallengeFamilyCount();

        // 비율 계산
        double percentage = (double) familyContribution / 120 * 100;

        // 행운이 상태 결정
        if (percentage >= 75) {
            return 3; // 행목
        } else if (percentage >= 25) {
            return 2; // 행운
        } else {
            return 1; // 새싹
        }

    }

}
