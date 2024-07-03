package ongjong.namanmoo.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.dto.family.FamilyInviteDto;
import ongjong.namanmoo.dto.family.FamilyMemberDto;
import ongjong.namanmoo.response.BaseResponse;
import ongjong.namanmoo.service.FamilyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/family")
public class FamilyApiController {

    private final FamilyService familyService;

    /**
     * 가족 생성
     */
    // TODO: 현재 Member role에 입력받은 role을 넣어줘야 한다.
    @PostMapping("/create")
    public BaseResponse<String> createFamily(@RequestBody CreateFamilyRequest request) {
        Family family = familyService.createFamily(request.getFamilyName(), request.getFamilySize(), request.getOwnerRole());
        return new BaseResponse<>("200", "Family created successfully", family.getInviteCode());
    }

    /**
     * 가족 참여
     */
    @PostMapping("/join")
    public BaseResponse<String> joinFamily(@RequestBody JoinFamilyRequest request) {
        familyService.addMemberToFamily(request.getFamilyId(), request.getRole());
        return new BaseResponse<>("200", "Join Family Success", request.getFamilyId().toString());
    }

    /**
     * 초대 코드 처리
     */
    @GetMapping("/family")
    public BaseResponse<FamilyInviteDto> handleInvite(@RequestParam("code") String code) {
        Optional<Family> family = familyService.findFamilyByInviteCode(code);
        if (family.isPresent()) {
            FamilyInviteDto familyInviteDto = new FamilyInviteDto(family.get());
            return new BaseResponse<>("200", "Get Family Info Success.", familyInviteDto);
        } else {
            return new BaseResponse<>("404", "Invite code not found", null);
        }
    }

    @GetMapping("/family/info") // TODO: 이부분 url /family/my 라고 되어있기는 한데 잘 모르겠어서 일단 이렇게 했음
    public BaseResponse<List<FamilyMemberDto>> getFamilyInfo(@RequestParam("familyId") String familyId) {
        List<FamilyMemberDto> members = familyService.getFamilyMembersInfo(familyId);
        return new BaseResponse<>("200", "Get Family Info Success.", members);
    }

    @Getter
    public static class CreateFamilyRequest {
        private int familySize;
        private String familyName;
        private String ownerRole;
    }

    @Getter
    public static class JoinFamilyRequest {
        private Long familyId;
        private String role;
    }
}