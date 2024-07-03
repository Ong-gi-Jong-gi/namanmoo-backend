package ongjong.namanmoo.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import ongjong.namanmoo.domain.Member;

public record MemberSignUpDto(
        @NotBlank(message = "아이디를 입력해주세요")
        String loginId,
        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,30}$",
                message = "비밀번호는 8~30 자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다.")
        String password,
        @NotBlank(message = "이름을 입력해주세요")
        String name,
        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickName) {

    public Member toEntity() {
        return Member.builder().loginId(loginId).password(password).name(name).nickName(nickName).build();
    }
}
