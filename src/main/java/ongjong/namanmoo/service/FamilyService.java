package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.family.FamilyMemberDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final Random RANDOM = new SecureRandom();


    @Transactional
    public Family createFamily(String familyName, int maxFamilySize, String ownerRole) {

        // 현재 로그인한 유저 정보를 가져옴
        String loginId = SecurityUtil.getLoginLoginId();
        Member familyOwner = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + loginId));

        Family family = new Family();
        family.setFamilyName(familyName);
        family.setMaxFamilySize(maxFamilySize);
        family.setFamilyOwnerId(familyOwner.getMemberId());

        // 초대 코드 생성 및 중복 체크
        generateUniqueInviteCode(family);
        familyRepository.save(family);

        // 가족 소유자의 역할 설정
        familyOwner.setRole(ownerRole);
        familyOwner.setFamily(family);
        memberRepository.save(familyOwner);


        return family;
    }


    // 내 가족 정보 확인
    public List<FamilyMemberDto> getFamilyMembersInfo() {
        String currentLoginId = SecurityUtil.getLoginLoginId();
        Member currentUser = memberRepository.findByLoginId(currentLoginId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found for loginId: " + currentLoginId));

        Long familyId = currentUser.getFamily().getFamilyId();
        List<Member> members = memberRepository.findByFamilyFamilyId(familyId);
        return convertMembersToDto(members);
    }

//    // 초대 URL 생성
//    public String createInviteUrl(Family family) {
//        String inviteCode = family.getInviteCode();
//        return "https://localhost/family?code=" + inviteCode;
//    }

    // 가족에 멤버 추가
    @Transactional
    public void addMemberToFamily(Long familyId, String role) {
        String loginId = SecurityUtil.getLoginLoginId();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with loginId: " + loginId));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found with id: " + familyId));

        long currentFamilySize = memberRepository.countByFamilyId(familyId);
//        log.info("currentFamilySize : {}", currentFamilySize);

        if (currentFamilySize >= family.getMaxFamilySize()) {
            throw new IllegalStateException("Family is already full");
        }


        member.setFamily(family);
        member.setRole(role);

        memberRepository.save(member);
        familyRepository.save(family);
    }


    // 엔티티를 DTO로 변환하는 메서드
    private FamilyMemberDto convertToDto(Member member) {
        FamilyMemberDto dto = new FamilyMemberDto();
        dto.setUserId(member.getLoginId());
        dto.setName(member.getName());
        dto.setNickname(member.getNickname());
        dto.setRole(member.getRole());
        dto.setUserImg(member.getMemberImage());
        return dto;
    }

    // 엔티티 리스트를 DTO리스트로 변환
    private List<FamilyMemberDto> convertMembersToDto(List<Member> members) {
        return members.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // 초대 코드 생성 및 중복 체크
    public void generateUniqueInviteCode(Family family) {
        boolean isUnique = false;
        String inviteCode = null;

        while (!isUnique) {
            inviteCode = generateInviteCode();
            isUnique = !familyRepository.existsByInviteCode(inviteCode);
        }

        //가족에 초대코드 저장
        family.setInviteCode(inviteCode);
    }

    // 초대 코드 생성
    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}
