package ongjong.namanmoo.service;


import lombok.RequiredArgsConstructor;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.*;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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
        Long number = findCurrentChallengeNum(familyId);
        if (number == null) {
            return null;
        }
        return challengeRepository.findByChallengeNum(number);
    }


    public boolean join(Long familyId){     // 캐릭터 생성
        Optional<Family> familyOptional = familyRepository.findById(familyId);
        if (familyOptional.isPresent()) {
            Family family = familyOptional.get();
            Lucky lucky1 = new Lucky();
            lucky1.setFamily(family);
            lucky1.setStatus(1);
            lucky1.setChallengeStartDate(new Timestamp(System.currentTimeMillis()));
//            lucky1.setCurrentChallengeNumber(1L);       // 현재 진행하고있는 challenge에 따라 current challenge가 바뀌어야함
            luckyRepository.save(lucky1);
            return true;
        } else {
            return false;
        }
    }

    public boolean createAnswer(Long familyId){

        List<Member> members = memberRepository.findByFamilyFamilyId(familyId);
        List<Challenge> challenges = challengeRepository.findAll();
        if (members.isEmpty()) {
            return false; // 가족에 해당하는 회원이 없으면 false 반환
        }
        for (Member member : members){
        // 해당 가족에 해당하는 인원의 id를 가지는 answer 생성
            for (Challenge challenge : challenges){      // 일단 normal만타입 받음 나머지도 추가해야함
                if (challenge instanceof NormalC){
                    NormalA normal = new NormalA();
                    normal.setMember(member);
                    normal.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    normal.setCheckChallenge(false);
                    normal.setChallenge(challenge);
                    answerRepository.save(normal);
                }
                else if (challenge instanceof GroupC){
                    GroupA group = new GroupA();
                    group.setMember(member);
                    group.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    group.setCheckChallenge(false);
                    group.setChallenge(challenge);
                    answerRepository.save(group);
                }
                else if (challenge instanceof FaceTimeC){
                    FaceTimeA facetime = new FaceTimeA();
                    facetime.setMember(member);
                    facetime.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    facetime.setCheckChallenge(false);
                    facetime.setChallenge(challenge);
                    answerRepository.save(facetime);
                }
                else if (challenge instanceof PhotoC){
                    PhotoA Photo = new PhotoA();
                    Photo.setMember(member);
                    Photo.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    Photo.setCheckChallenge(false);
                    Photo.setChallenge(challenge);
                    answerRepository.save(Photo);
                }
                else if (challenge instanceof VoiceC){
                    VoiceA voice = new VoiceA();
                    voice.setMember(member);
                    voice.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    voice.setCheckChallenge(false);
                    voice.setChallenge(challenge);
                    answerRepository.save(voice);
                }
            }
        }
        return true;
    }

    public Long findCurrentChallengeNum(Long familyId) {       // 현재 진행중인 challenge 번호 조회
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(familyId);
//        for (Lucky lucky : luckies) {
//            if (lucky.getCurrentChallengeNumber() != 31) {
//                 return lucky.getCurrentChallengeNumber(); // 현재 진행되어야할 challenge를 반환
//            }
//        }
        return null;
    }

    public List<Challenge> findChallenges(Long familyId) {      // 현재 진행한 챌린지 리스트 가져오기
        Long number = findCurrentChallengeNum(familyId);
        if (number == null) {
            return null;
        }
        return challengeRepository.findByChallengeNumLessThanEqual(number);
    }

    public boolean findIsCompleteAnswer(Challenge challenge,Member member){
        return answerRepository.findByChallengeAndMember(challenge, member)
                .map(Answer::isCheckChallenge)      // answer가 존재할 경우 ischeckChallenge 값을 반환
                .orElse(false);
    }
}
