package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.family.FamilyMemberDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import ongjong.namanmoo.dto.family.FamilyInviteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final Random RANDOM = new SecureRandom();
    private final LuckyRepository luckyRepository;


    // 가족 생성
    @Override
    public Family createFamily(String familyName, int maxFamilySize, String ownerRole) {

        // 현재 로그인한 유저 정보를 가져옴
        String loginId = SecurityUtil.getLoginLoginId();

        Member familyOwner = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + loginId));

        // 이미 가족에 속해 있는지 확인
        if (familyOwner.getFamily() != null) {
            throw new IllegalStateException("User already belongs to a family");
        }

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
    @Override
    @Transactional(readOnly = true)
    public List<FamilyMemberDto> getFamilyMembersInfo() {
        String currentLoginId = SecurityUtil.getLoginLoginId();
        Member currentUser = memberRepository.findByLoginId(currentLoginId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found for loginId: " + currentLoginId));
        if (currentUser.getFamily() == null) {
            return null;
        }
        Long familyId = currentUser.getFamily().getFamilyId();
        List<Member> members = memberRepository.findByFamilyFamilyId(familyId);
        return convertMembersToDto(members);
    }

//    // 내 가족 코드 확인
//    @Override
//    @Transactional(readOnly = true)
//    public String getInviteCode() {
//        String currentLoginId = SecurityUtil.getLoginLoginId();
//        Member currentUser = memberRepository.findByLoginId(currentLoginId)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found for loginId: " + currentLoginId));
//        Family family = currentUser.getFamily();
//        if (family == null)
//            return "null";
//        return family.getInviteCode();
//    }

//    // 초대 URL 생성
//    public String createInviteUrl(Family family) {
//        String inviteCode = family.getInviteCode();
//        return "https://localhost/family?code=" + inviteCode;
//    }

    // 초대 코드로 가족 정보 확인
    @Override
    @Transactional(readOnly = true)
    public FamilyInviteResponse getFamilyInfoByInviteCode(String inviteCode) {
        Family family = familyRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Family not found with invite code: " + inviteCode));

        List<Member> members = memberRepository.findByFamilyFamilyId(family.getFamilyId());
        List<FamilyMemberDto> memberDtos = convertMembersToDto(members);

        return new FamilyInviteResponse(family.getFamilyName(), family.getFamilyId().toString(), memberDtos);
    }

    // 가족에 멤버 추가
    @Override
    public void addMemberToFamily(Long familyId, String role) {
        String loginId = SecurityUtil.getLoginLoginId();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with loginId: " + loginId));

        // 이미 가족에 속해 있는지 확인
        if (member.getFamily() != null) {
            throw new IllegalStateException("User already belongs to a family");
        }

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found with id: " + familyId));

        long currentFamilySize = memberRepository.countByFamilyId(familyId);
        if (currentFamilySize >= family.getMaxFamilySize()) {
            throw new IllegalStateException("Family is already full");
        }

        // Role validation
        if (!(role.equals("아빠") || role.equals("엄마") || role.equals("아들") || role.equals("딸"))) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        member.setFamily(family);
        member.setRole(role);

        memberRepository.save(member);
        familyRepository.save(family);
    }

    // 로그인 아이디로 패밀리 아이디 찾기
    @Override
    @Transactional(readOnly = true)
    public Long findFamilyId() {
        String currentLoginId = SecurityUtil.getLoginLoginId();
        Member currentUser = memberRepository.findByLoginId(currentLoginId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found for loginId: " + currentLoginId));

        return currentUser.getFamily().getFamilyId();
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
