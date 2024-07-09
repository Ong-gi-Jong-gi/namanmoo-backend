package ongjong.namanmoo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.dto.member.LoginRequestDto;
import ongjong.namanmoo.dto.member.MemberInfoDto;
import ongjong.namanmoo.dto.member.MemberSignUpDto;
import ongjong.namanmoo.dto.member.MemberUpdateDto;
import ongjong.namanmoo.dto.member.UpdatePasswordDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.AwsS3Service;
import ongjong.namanmoo.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AwsS3Service awsS3Service;

    // 회원 가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody MemberSignUpDto memberSignUpDto) throws Exception {
        memberService.signUp(memberSignUpDto);
        ApiResponse<Void> response = new ApiResponse<>("200", "Sign up Success", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 아이디 중복 체크
    @PostMapping("/signup/duplicate")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> duplicate(@Valid @RequestBody LoginRequestDto loginRequestDto) throws Exception {
        boolean isDuplicate = memberService.isDuplicateId(loginRequestDto);
        Map<String, Boolean> data = new HashMap<>();
        data.put("isAvailable", !isDuplicate);

        ApiResponse<Map<String, Boolean>> response = new ApiResponse<>(
                "200",
                isDuplicate ? "ID is not available" : "ID is available",
                data
        );
        HttpStatus status = isDuplicate ? HttpStatus.CONFLICT : HttpStatus.OK;
        return new ResponseEntity<>(response, status);
    }

    // 회원 정보 수정
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<MemberInfoDto> updateBasicInfo(@RequestPart("userInfo") MemberUpdateDto memberUpdateDto) throws Exception {

        String uploadImageUrl = null;

        // 파일을 전송했을 경우에만 S3 파일 업로드 수행
        if (memberUpdateDto.userImg().isPresent() && !memberUpdateDto.userImg().get().isEmpty()) {
            uploadImageUrl = awsS3Service.uploadFile(memberUpdateDto.userImg().get());
        }

        memberService.update(memberUpdateDto);

        // MemberService의 update 메소드에서 imagePath를 갱신해야 합니다.
        MemberInfoDto info = memberService.getMyInfo();
        return new ApiResponse<>("200", "Update User Info Success", info);
    }


    // 비밀 번호 수정
    @PostMapping("/users/password")
    @ResponseStatus(HttpStatus.OK)
    public void updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordDto) throws Exception {
        memberService.updatePassword(updatePasswordDto.checkPassword(), updatePasswordDto.toBePassword());
    }

    // 내 정보 조회
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<MemberInfoDto>> getMyInfo() throws Exception {
        MemberInfoDto info = memberService.getMyInfo();
        ApiResponse<MemberInfoDto> response = new ApiResponse<>("200", "Get User Info Success", info);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}