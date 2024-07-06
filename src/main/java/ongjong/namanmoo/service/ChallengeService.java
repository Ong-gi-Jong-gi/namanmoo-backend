package ongjong.namanmoo.service;


import lombok.RequiredArgsConstructor;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.*;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeService {

    private final FamilyRepository familyRepository;
    private final ChallengeRepository challengeRepository;
    private final LuckyRepository luckyRepository;
    private final MemberRepository memberRepository;
    private final AnswerRepository answerRepository;


    // familyId를 통해 해당 날짜에 해당하는 오늘의 challenge 조회
    // 해당 가족 id를 가지고 있는 행운이 모두 조회
    // 행운이들 중 오늘의 챌린지 값이 30이 아닌 행운이의 오늘의 챌린지 값을 가져와야한다.
    @Transactional(readOnly = true)
    public Challenge findCurrentChallenge(Long familyId) {
        Long number = findCurrentChallengeNum(familyId);
        if (number == null) {
            return null;
        }
        return challengeRepository.findByChallengeNum(number);
    }

    @Transactional(readOnly = true)
    public Long findCurrentChallengeNum(Long familyId) {       // 현재 진행중인 challenge 번호 조회
        List<Lucky> luckies = luckyRepository.findByFamilyId(familyId);
        for (Lucky lucky : luckies) {
            if (lucky.getCurrentChallengeNum() != -1) {
                 return lucky.getCurrentChallengeNum(); // 현재 진행되어야할 challenge를 반환
            }
        }
        return null;
    }
    @Transactional(readOnly = true)
    public List<Challenge> findChallenges() throws Exception{      // 현재 진행한 챌린지 리스트 가져오기
        Member member = findMemberByLoginId();  // 로그인한 member
        Family family = member.getFamily();

        Long number = findCurrentChallengeNum(family.getId());      // 진행하는 challenge 번호
        if (number == null) {
            return null;
        }
        return challengeRepository.findByChallengeNumLessThanEqual(number);
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
    public Challenge findChallengeByMemberId() throws Exception{      // 회원 아이디로 회원 조회
        Member member = findMemberByLoginId();  // 로그인한 member
        Family family = member.getFamily();

        int currentFamilySize = memberRepository.countByFamilyId(family.getId());
        if (currentFamilySize != family.getMaxFamilySize()) {
            return null;        // 현재 가족의수 가 max가족의 수와 같지 않을 겨우 오늘의 챌린지 조회 실패 -> null반환
        }

        List<Lucky> luckies = luckyRepository.findByFamilyId(family.getId());
        if (luckies.isEmpty()) {
            return null; // luckies 리스트가 비어있을 경우 null 반환
        }

        boolean validLuckyExists = luckies.stream()
                .anyMatch(lucky -> lucky.getCurrentChallengeNum() != -1);
        if (!validLuckyExists) {
            return null;        // 진행중인 챌린지 , lucky가 없을 경우
        }

        Challenge challenge = findCurrentChallenge(member.getFamily().getId());     //familyId를 통해 오늘의 챌린지 조회
        return challenge;
    }

}
