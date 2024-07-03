package ongjong.namanmoo.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TestDataInitService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    @PostConstruct
    public void init() {
        createTestFamily("Test Family 1", "testcode1");
//        createTestFamily("Test Family 2", "testcode2");
    }

    private void createTestFamily(String familyName, String inviteCode) {
        Family family = new Family();
        family.setFamilyName(familyName);
        family.setMaxFamilySize(4);
        family.setCurrentFamilySize(4);
        family.setInviteCode(inviteCode);
        family.setChallengeFamilyCount(0L);

        family = familyRepository.save(family);

        // Family에 속하는 4명의 멤버 생성 및 저장
        createMembers(family);
    }

    private void createMembers(Family family) {
        for (int i = 1; i <= 4; i++) {
            Member member = new Member();
            member.setFamily(family);
            member.setLoginId(family.getFamilyName().toLowerCase().replace(" ", "") + "member" + i);
            member.setPassword("password" + i);
            member.setName(family.getFamilyName() + " Member " + i);
            member.setRole("USER");
            member.setNickname("Nickname " + i);
            member.setChallengeMemberCount(0L);
            member.setCheckChallenge(false);
            member.setMemberImage("image" + i + ".png");
            memberRepository.save(member);
        }
    }
}
