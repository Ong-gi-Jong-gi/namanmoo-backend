package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        List<Lucky> luckyList = luckyRepository.findByFamilyFamilyId(familyId);
        boolean hasRunningLucky = luckyList.stream().anyMatch(Lucky::isRunning); // lucky의 running 컬럼이 true가 없을 경우에만 lucky를 생성  todo 이거 추가해야함
        if (familyOptional.isPresent() && !hasRunningLucky) {
            Family family = familyOptional.get();
            Lucky lucky1 = new Lucky();
            lucky1.setFamily(family);
            lucky1.setStatus(1L);
            String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));  //todo 여기 날짜 받아온걸로 바꿔야함
            lucky1.setChallengeStartDate(currentDateStr); // 문자열 형식으로 날짜 저장
            lucky1.setRunning(true);       // 현재 진행하고있는 challenge에 따라 current challenge가 바뀌어야함 // 챌린지를 다시 시작할 경우 1이아닌 31이 될 수도 있어야함
            luckyRepository.save(lucky1);
            return true;
        } else {
            return false;
        }
    }


}
