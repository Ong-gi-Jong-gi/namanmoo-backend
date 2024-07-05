package ongjong.namanmoo.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.dto.family.FamilyInviteDto;
import ongjong.namanmoo.dto.family.FamilyMemberDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.response.CreateFamilyResponse;
import ongjong.namanmoo.response.FamilyInfoResponse;
import ongjong.namanmoo.response.JoinFamilyResponse;
import ongjong.namanmoo.service.FamilyService;
import org.springframework.http.ResponseEntity;
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
    public ApiResponse<CreateFamilyResponse> createFamily(@RequestBody CreateFamilyRequest request) {
        Family family = familyService.createFamily(request.getFamilyName(), request.getFamilySize(), request.getOwnerRole());
        CreateFamilyResponse response = new CreateFamilyResponse(family.getInviteCode());
        return new ApiResponse<>("200", "Family created successfully", response);
    }

    /**
     * 가족 참여
     */
    @PostMapping("/join")
    public ApiResponse<JoinFamilyResponse> joinFamily(@RequestBody JoinFamilyRequest request) {
        familyService.addMemberToFamily(request.getFamilyId(), request.getRole());
        JoinFamilyResponse response = new JoinFamilyResponse(request.getFamilyId().toString());
        return new ApiResponse<>("200", "Join Family Success", response);
    }

    /**
     * 초대 코드 처리
     */
//    @GetMapping
//    public ApiResponse<FamilyInviteDto> getFamilyInfoByInviteCode(@RequestParam(name = "code") String inviteCode) {
//        Optional<Family> family = familyService.findFamilyByInviteCode(inviteCode);
//        if (family.isPresent()) {
//            FamilyInviteDto familyInviteDto = new FamilyInviteDto(family.get());
//            return new ApiResponse<>("200", "Get Family Info Success.", familyInviteDto);
//        } else {
//            return new ApiResponse<>("404", "Invite code not found", null);
//        }
//    }

    /**
     * 내 가족 조회
     */
    @GetMapping("/info") // TODO: 이부분 url /info 라고 하는게 더 좋아보임
    public ResponseEntity<ApiResponse<FamilyInfoResponse>> getFamilyInfo() {
        List<FamilyMemberDto> members = familyService.getFamilyMembersInfo();
        FamilyInfoResponse familyInfoResponse = new FamilyInfoResponse(members);

        ApiResponse<FamilyInfoResponse> response = new ApiResponse<>(
                "200",
                "Get Family Info Success.",
                familyInfoResponse
        );
        return ResponseEntity.ok(response);
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