package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LuckyService {

    private final LuckyRepository luckyRepository;
    private final FamilyRepository familyRepository;

    public boolean join(Long familyId){     // 캐릭터 생성
        Optional<Family> familyOptional = familyRepository.findById(familyId);
        if (familyOptional.isPresent()) {
            Family family = familyOptional.get();
            Lucky lucky1 = new Lucky();
            lucky1.setFamily(family);
            lucky1.setStatus(1L);
            lucky1.setChallengeStartDate(new Timestamp(System.currentTimeMillis()));
            lucky1.setCurrentChallengeNum(1L);       // 현재 진행하고있는 challenge에 따라 current challenge가 바뀌어야함
            luckyRepository.save(lucky1);
            return true;
        } else {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Lucky findCurrentLucky(Long familyId) {       // 현재 진행중인 lucky id 조회
        List<Lucky> luckies = luckyRepository.findByFamilyId(familyId);
        for (Lucky lucky : luckies) {
            if (lucky.getCurrentChallengeNum() != -1) {
                return lucky; // 현재 진행되고있는 luckyid 반환
            }
        }
        return null;
    }
}
