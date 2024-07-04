package ongjong.namanmoo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.dto.member.MemberInfoDto;
import ongjong.namanmoo.dto.member.MemberSignUpDto;
import ongjong.namanmoo.dto.member.MemberUpdateDto;
import ongjong.namanmoo.dto.member.UpdatePasswordDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원 가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody MemberSignUpDto memberSignUpDto) throws Exception {
        memberService.signUp(memberSignUpDto);
        ApiResponse<Void> response = new ApiResponse<>("200", "Sign up Success", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원 정보 수정
    @PostMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<MemberInfoDto> updateBasicInfo(@Valid @RequestBody MemberUpdateDto memberUpdateDto) throws Exception {
        memberService.update(memberUpdateDto);
        MemberInfoDto info = memberService.getMyInfo();
        return new ApiResponse<>("200", "Update User Info Success", info);
    }

    // 비밀 번호 수정
    @PostMapping("/user/password")
    @ResponseStatus(HttpStatus.OK)
    public void updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordDto) throws Exception {
        memberService.updatePassword(updatePasswordDto.checkPassword(), updatePasswordDto.toBePassword());
    }

    // 내 정보 조회
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<MemberInfoDto>> getMyInfo() throws Exception {
        MemberInfoDto info = memberService.getMyInfo();
        ApiResponse<MemberInfoDto> response = new ApiResponse<>("200", "Get User Info Success", info);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}