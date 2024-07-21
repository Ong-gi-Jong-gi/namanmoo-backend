package ongjong.namanmoo.service;

import ongjong.namanmoo.repository.ChallengeRepository;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
    @Transactional
    public class ChallengeServiceImplTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private LuckyRepository luckyRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    public void testChallengeService() {

    }
}
