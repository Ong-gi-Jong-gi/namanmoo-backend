package ongjong.namanmoo.service;


import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.dto.family.FamilyMemberDto;
import ongjong.namanmoo.dto.family.FamilyInviteResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FamilyService {


    Family createFamily(String familyName, int maxFamilySize, String ownerRole);

    // 내 가족 정보 확인
    List<FamilyMemberDto> getFamilyMembersInfo();

    // 내 가족 코드 확인
    String getInviteCode();

    // 초대 코드로 가족 정보 확인
    FamilyInviteResponse getFamilyInfoByInviteCode(String inviteCode);

    // 가족에 멤버 추가
    void addMemberToFamily(Long familyId, String role);

    Long findFamilyId();
}
