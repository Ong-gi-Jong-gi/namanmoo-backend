package ongjong.namanmoo.dto.member;

import jakarta.validation.constraints.NotBlank;

public record MemberWithdrawDto(@NotBlank(message = "비밀번호를 입력해주세요")
                                String checkPassword) {
}
