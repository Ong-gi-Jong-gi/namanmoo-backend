package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class FamilyRepositoryTest {

    @Autowired
    private FamilyRepository familyRepository;
    private LuckyRepository luckyRepository;

    @Test
    @Transactional
//    @Rollback(false)
    public void testfamily() throws Exception{
        Family family = new Family();
        family.setFamilyName("family a");
        family.setFamilyOwnerId(1L);
        family.setChallengeFamilyCount(0L);
        family.setMaxFamilySize(4);
        family.setInviteCode("0000");
        Long savedId = familyRepository.save(family).getId();
        Family findFamily = familyRepository.findById(savedId).get();
        Assertions.assertThat(findFamily.getId()).isEqualTo(family.getId());
        Assertions.assertThat(findFamily.getFamilyName()).isEqualTo(family.getFamilyName());
        Assertions.assertThat(findFamily).isEqualTo(family);
    }

}
