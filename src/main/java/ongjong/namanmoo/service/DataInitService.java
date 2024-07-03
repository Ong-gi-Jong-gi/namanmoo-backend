package ongjong.namanmoo.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.NormalC;
import ongjong.namanmoo.repository.ChallengeRepository;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@Transactional
@RequiredArgsConstructor
public class DataInitService {

    private final FamilyRepository familyRepository;
    private final ChallengeRepository challengeRepository;
    private final MemberRepository memberRepository;

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

        Family family2 = new Family();
        family2.setFamilyName("Test Family 2");
        family2.setMaxFamilySize(4L);
        family2.setCurrentFamilySize(4L);
        family2.setInviteCode("testcode2");
        family2.setChallengeFamilyCount(0L);
        family2.setFamilyOwnerId(1L);
        family2 = familyRepository.save(family2);

        // Challenge 생성 및 저장
        createChallenges();

        // Family 1에 속하는 5명의 멤버 생성 및 저장
        createMembers(family1);
    }

    private void createChallenges() {
        String[] challengeDescriptions = {
                "부모님에게 연락하기",
                "가족에게 가장 미안했던 순간은?",
                "가족과 함께 저녁 식사하기",
                "가족과 함께 사진 찍기",
                "가족과 함께 산책하기"
        };

        for (int i = 0; i < challengeDescriptions.length; i++) {
            NormalC challenge = new NormalC();
            challenge.setChallengeNum((long) (i + 1));
            challenge.setNormalChallenge(challengeDescriptions[i]);
            challengeRepository.save(challenge);
        }
    }

    private void createMembers(Family family) {
        for (int i = 1; i <= 4; i++) {
            Member member = new Member();
            member.setFamily(family);
            member.setLoginId("member" + i);
            member.setPassword("password" + i);
            member.setName("Member " + i);
            member.setRole("USER");
            member.setNickname("Nickname " + i);
            member.setChallengeMemberCount(0L);
            member.setCheckChallenge(false);
            member.setMemberImage("image" + i + ".png");
            memberRepository.save(member);
        }
    }
}
