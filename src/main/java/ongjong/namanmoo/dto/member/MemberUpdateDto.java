package ongjong.namanmoo.dto.member;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public record MemberUpdateDto(
        Optional<String> name,
        Optional<String> nickname,
        Optional<String> role
) {
}