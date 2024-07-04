package ongjong.namanmoo.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.dto.family.FamilyInviteDto;
import ongjong.namanmoo.dto.family.FamilyMemberDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.FamilyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/family")
public class FamilyController {

    private final FamilyService familyService;

    /**
     * 가족 생성
     */
    @PostMapping("/create")
    public ApiResponse<String> createFamily(@RequestBody CreateFamilyRequest request) {
        Family family = familyService.createFamily(request.getFamilyName(), request.getFamilySize(), request.getOwnerRole());
        return new ApiResponse<>("200", "Family created successfully", family.getInviteCode());
    }

    /**
     * 가족 참여
     */
    @PostMapping("/join")
    public ApiResponse<String> joinFamily(@RequestBody JoinFamilyRequest request) {
        familyService.addMemberToFamily(request.getFamilyId(), request.getRole());
        return new ApiResponse<>("200", "Join Family Success", request.getFamilyId().toString());
    }

    /**
     * 초대 코드 처리
     */
    @GetMapping
    public ApiResponse<FamilyInviteDto> handleInvite(@RequestParam("code") String code) {
        Optional<Family> family = familyService.findFamilyByInviteCode(code);
        if (family.isPresent()) {
            FamilyInviteDto familyInviteDto = new FamilyInviteDto(family.get());
            return new ApiResponse<>("200", "Get Family Info Success.", familyInviteDto);
        } else {
            return new ApiResponse<>("404", "Invite code not found", null);
        }
    }

    /**
     * 내 가족 조회
     */
    @GetMapping("/my") // TODO: 이부분 url /info 라고 하는게 더 좋아보임
    public ApiResponse<List<FamilyMemberDto>> getFamilyInfo(@RequestBody FamilyIdRequest familyId) {
        List<FamilyMemberDto> members = familyService.getFamilyMembersInfo(familyId.getFamilyId());
        return new ApiResponse<>("200", "Get Family Info Success.", members);
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

    @Getter
    public static class FamilyIdRequest {
        private String familyId;
    }
}