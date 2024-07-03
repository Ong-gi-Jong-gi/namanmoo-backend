package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
public class FamilyServiceTest {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private FamilyRepository familyRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void createFamily() throws Exception {
        //given
        String familyName = "Test Family";
        int maxFamilySize = 4;
//        Long familyOwnerId = 1L;
        String familyRole = "딸";

        //when
        Family createdFamily = familyService.createFamily(familyName, maxFamilySize, familyRole);

        //then
        Optional<Family> foundFamily = familyRepository.findById(createdFamily.getFamilyId());
        assertThat(foundFamily).isPresent();
        assertThat(foundFamily.get().getFamilyName()).isEqualTo(familyName);
        assertThat(foundFamily.get().getMaxFamilySize()).isEqualTo(maxFamilySize);
//        assertThat(foundFamily.get().getFamilyOwnerId()).isEqualTo(familyOwnerId);
        Member owner = memberRepository.findById(foundFamily.get().getFamilyOwnerId()).orElseThrow();
        assertThat(owner.getRole()).isEqualTo(familyRole);
        assertThat(foundFamily.get().getInviteCode()).isNotEmpty();
    }

    @Test
    public void findFamilyByInviteCode() throws Exception {
        //given
        String familyName = "Test Family";
        int maxFamilySize = 4;
        String familyRole = "엄마";
        Family createdFamily = familyService.createFamily(familyName, maxFamilySize, familyRole);
        String inviteCode = createdFamily.getInviteCode();

        //when
        Optional<Family> foundFamily = familyService.findFamilyByInviteCode(inviteCode);

        //then
        assertThat(foundFamily).isPresent();
        assertThat(foundFamily.get().getInviteCode()).isEqualTo(inviteCode);
    }
}