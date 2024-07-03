package ongjong.namanmoo.service;


import lombok.RequiredArgsConstructor;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.answer.NormalA;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public Challenge findCurrentChallenge(Long familyId) {
        // 해당 가족 ID를 가지고 있는 모든 Object 조회
        List<Lucky> luckies = luckyRepository.findByFamilyId(familyId);

        // currentChallengeNumber가 30이 아닌 Object 찾기
        for (Lucky lucky : luckies) {
            if (lucky.getCurrentChallengeNumber() != 30) {
                return challengeRepository.findByChallengeNum(lucky.getCurrentChallengeNumber()); // 현재 진행되어야할 challenge를 반환
            }
        }
        // 조건에 맞는 Object가 없는 경우
        return null;
    }

    public boolean join(Long familyId){     // 캐릭터 생성
        Optional<Family> familyOptional = familyRepository.findById(familyId);
        if (familyOptional.isPresent()) {
            Family family = familyOptional.get();
            Lucky lucky1 = new Lucky();
            lucky1.setFamily(family);
            lucky1.setStatus(1L);
            lucky1.setChallengeStartDate(new Timestamp(System.currentTimeMillis()));
            lucky1.setCurrentChallengeNumber(2L);       // 현재 진행하고있는 challenge에 따라 current challenge가 바뀌어야함
            luckyRepository.save(lucky1);
            return true;
        } else {
            return false;
        }
    }

    public boolean createAnswer(Long familyId){
        System.out.println("bbbbb");
        List<Member> members = memberRepository.findByFamilyId(familyId);
        if (members.isEmpty()) {
            System.out.println("vcccccccccc");
            return false; // 가족에 해당하는 회원이 없으면 false 반환
        }
        for (Member member : members){
            System.out.println("aaaaaaaa");
//            Long id = member.getMemberId(); // 해당 가족에 해당하는 인원의 id를 가지는 answer 생성
            for (long i =1 ; i <= 5; i++){
                NormalA normal = new NormalA();
                normal.setMember(member);
                normal.setCreateDate(new Timestamp(System.currentTimeMillis()));

                Challenge challenge = challengeRepository.findByChallengeNum(i);
                if (challenge == null){
                    System.out.println("Fwfwwfw");
                }
                normal.setChallenge(challenge);

                answerRepository.save(normal);
            }
        }
        return true;
    }

//
//    public List<Challenge> findChallenges(Long familyId) {       // challenge 리스트 조회
//
//        return challengeRepository.findAll();
//    }
}
