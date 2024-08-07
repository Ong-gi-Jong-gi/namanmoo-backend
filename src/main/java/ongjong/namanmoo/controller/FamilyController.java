package ongjong.namanmoo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.dto.family.*;
import ongjong.namanmoo.service.FamilyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/family")
public class FamilyController {

    private final FamilyService familyService;

    /**
     * 가족 생성
     */
    @PostMapping
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
    @GetMapping
    public ResponseEntity<ApiResponse<FamilyInviteResponse>> getFamilyInfo(@RequestParam("code") String inviteCode) {
        FamilyInviteResponse familyInfo = familyService.getFamilyInfoByInviteCode(inviteCode);
        ApiResponse<FamilyInviteResponse> response = new ApiResponse<>(
                "200",
                "Get Family Info Success.",
                familyInfo
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 내 가족 조회
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<FamilyInfoResponse>> getFamilyInfo(){
        List<FamilyMemberDto> members = familyService.getFamilyMembersInfo();
        if(members == null) {
            ApiResponse<FamilyInfoResponse> response = new ApiResponse<>(
                    "404",
                    "family not found",
                    null
            );
            return ResponseEntity.ok(response);
        }

        FamilyInfoResponse familyInfoResponse = new FamilyInfoResponse(members);
        ApiResponse<FamilyInfoResponse> response = new ApiResponse<>(
                "200",
                "Get Family Info Success.",
                familyInfoResponse
        );

        return ResponseEntity.ok(response);
    }

//    // 가족 코드 조회
//    @GetMapping("/code")
//    public ApiResponse<CreateFamilyResponse> getMyFamilyCode() {
//        CreateFamilyResponse inviteCode = new CreateFamilyResponse(familyService.getInviteCode());
//        return new ApiResponse<>("200", "Get InviteCode Success", inviteCode);
//
//    }
}