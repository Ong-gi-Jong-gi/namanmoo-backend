package ongjong.namanmoo.service;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.domain.challenge.NormalC;
import ongjong.namanmoo.repository.ChallengeRepository;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DataInitService {

    private final LuckyRepository luckyRepository;
    private final FamilyRepository familyRepository;
    private final ChallengeRepository challengeRepository;

    @PostConstruct
    public void init(){
        Family family1 = new Family();
        family1.setFamilyName("Test Family 1");
        family1.setMaxFamilySize(4L);
        family1.setCurrentFamilySize(4L);
        family1.setInviteCode("testcode1");
        family1.setChallengeFamilyCount(0L);
        family1.setFamilyOwnerId(1L);
        family1 = familyRepository.save(family1);

        // 첫 번째 Lucky 데이터 생성 및 저장
        Lucky lucky1 = new Lucky();
        lucky1.setFamily(family1);
        lucky1.setStatus(1L);
        lucky1.setChallengeStartDate(new Timestamp(System.currentTimeMillis()));
        lucky1.setCurrentChallengeNumber(1L);
        lucky1 = luckyRepository.save(lucky1);

        // 첫 번째 Challenge 데이터 생성 및 저장 (NormalC 타입)
        NormalC challenge1 = new NormalC();
        challenge1.setLucky(lucky1);
        challenge1.setChallengeNum(1L);
        challenge1.setNormalChallenge("부모님에게 연락하기");
        challenge1 = challengeRepository.save(challenge1);

        // 두 번째 Family 생성 및 저장
        Family family2 = new Family();
        family2.setFamilyName("Test Family 2");
        family2.setMaxFamilySize(4L);
        family2.setCurrentFamilySize(4L);
        family2.setInviteCode("testcode2");
        family2.setChallengeFamilyCount(0L);
        family2.setFamilyOwnerId(1L);
        family2 = familyRepository.save(family2);

        // 두 번째 Lucky 데이터 생성 및 저장
        Lucky lucky2 = new Lucky();
        lucky2.setFamily(family2);
        lucky2.setStatus(1L);
        lucky2.setChallengeStartDate(new Timestamp(System.currentTimeMillis()));
        lucky2.setCurrentChallengeNumber(1L);
        lucky2 = luckyRepository.save(lucky2);

        // 두 번째 Challenge 데이터 생성 및 저장 (NormalC 타입)
        NormalC challenge2 = new NormalC();
        challenge2.setLucky(lucky2);
        challenge2.setChallengeNum(2L);
        challenge2.setNormalChallenge("가족에게 가장 미안했던 순간은?");
        challenge2 = challengeRepository.save(challenge2);

        // Lucky와 Challenge 간의 관계 설정 및 저장
        lucky1.setChallenges(Collections.singletonList(challenge1));
        lucky2.setChallenges(Collections.singletonList(challenge2));
        luckyRepository.saveAll(List.of(lucky1, lucky2));
    }

}
