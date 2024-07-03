package ongjong.namanmoo.service;


import lombok.RequiredArgsConstructor;

import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.repository.ChallengeRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final LuckyRepository luckyRepository;

    // familyId를 통해 해당 날짜에 해당하는 오늘의 challenge 조회
    // 해당 가족 id를 가지고 있는 행운이 모두 조회
    // 행운이들 중 오늘의 챌린지 값이 30이 행운이의 오늘의 챌린지 값을 가져와야한다.

    public Challenge findCurrentChallenge(Long familyId) {
        // 해당 가족 ID를 가지고 있는 모든 Object 조회
        List<Lucky> luckies = luckyRepository.findByFamilyId(familyId);

        // currentChallengeNumber가 30이 아닌 Object 찾기
        for (Lucky lucky : luckies) {
            if (lucky.getCurrentChallengeNumber() != 30) {
                Long challengeId = lucky.getCurrentChallengeNumber();
                return challengeRepository.findById(challengeId).get(); // 현재 진행되어야할 challenge를 반환
            }
        }
        // 조건에 맞는 Object가 없는 경우
        return null;
    }

//    public Challenge findOne(Long familyId){    // 오늘의 챌린지 번호 반환
//        Long objectId = findCurrentObject(familyId);
//        Object object = objectRepository.findById(objectId);
//
//        return ;
//    }
//
//    public List<Challenge> findChallenges(Long familyId) {       // challenge 리스트 조회
//
//        return challengeRepository.findAll();
//    }
}
