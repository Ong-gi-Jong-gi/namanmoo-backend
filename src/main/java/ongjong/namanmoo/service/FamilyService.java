package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ongjong.namanmoo.repository.FamilyRepository.*;
import static ongjong.namanmoo.repository.FamilyRepository.findByInviteCode;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Family createFamily(String familyName, int maxFamilySize, Long familyOwnerId) {
        Family family = new Family();
        family.setFamilyName(familyName);
        family.setMaxFamilySize(maxFamilySize);
        family.setFamilyOwnerId(familyOwnerId); // TODO: 현재 MemberId가 familyOwnerId가 되도록 수정 필요한지 확인
        family.setCurrentFamilySize(1);

        // TODO: 현재 Member role에 입력받은 role을 넣어줘야 한다.
        family.getMembers().add(memberRepository.findById(familyOwnerId).orElse(null));

        // 초대 코드 생성 및 중복 체크
        generateUniqueInviteCode(family);

        return familyRepository.save(family);
    }

    private void generateUniqueInviteCode(Family family) {
        boolean isUnique = false;
        while (!isUnique) {
            family.generateInviteCode();
            isUnique = FamilyRepository.findByInviteCode(family.getInviteCode()).isEmpty();
        }
    }

    public String createInviteUrl(Family family) {
        String inviteCode = family.getInviteCode();
        return "https://localhost/family?code=" + inviteCode;
    }

    public Optional<Family> findFamilyByInviteCode(String inviteCode) {
        return FamilyRepository.findByInviteCode(inviteCode);
    }

    @Transactional
    public void addMemberToFamily(Long familyId, Long memberId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found with id: " + familyId));
        Member newMember = new Member();
        newMember.setMemberId(memberId);
        newMember.setFamily(family);
        family.getMembers().add(newMember);
        family.setCurrentFamilySize(family.getCurrentFamilySize() + 1);
        familyRepository.save(family);
    }

    public List<Family> findAll() {
        return familyRepository.findAll();
    }

    public Optional<Member> findMemberById(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
