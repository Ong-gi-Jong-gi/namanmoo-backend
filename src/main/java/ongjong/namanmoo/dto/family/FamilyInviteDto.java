package ongjong.namanmoo.dto.family;

import lombok.Data;
import ongjong.namanmoo.domain.Family;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class FamilyInviteDto {

    private String familyName;
    private String familyId;
    private List<FamilyMemberDto> members;

    public FamilyInviteDto(Family family) {
        this.familyName = family.getFamilyName();
        this.familyId = family.getFamilyId().toString();
        this.members = family.getMembers().stream().map(member -> {
            FamilyMemberDto memberDto = new FamilyMemberDto();
            memberDto.setName(member.getName());
            memberDto.setNickname(member.getNickname());
            memberDto.setRole(member.getRole());
            memberDto.setUserImg(member.getMemberImage());
            return memberDto;
        }).collect(Collectors.toList());
    }
}