package ongjong.namanmoo.domain;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.stream.Collectors;

import static ongjong.namanmoo.domain.MemberRole.*;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    @JsonBackReference // 순환 참조 방지, 자식 엔티티
    private Family family;

    @Column(nullable = false, unique = true)
    private String loginId; // 아이디

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String name; // 이름

    @Builder.Default
    private String role = "미정"; // 가족에서의 역할
//    @Enumerated(EnumType.STRING)
//    private MemberRole role = UNSPECIFIED;

    private String nickname; // 별명

    private String memberImage; // 프로필 사진(아직 정확히 모름)

    @Enumerated(EnumType.STRING)
    private LogInRole logInRole; // 권한 -> USER, ADMIN

    //== jwt 토큰 추가 ==//
    @Column(length = 1000)
    private String refreshToken;

    //== 정보 수정 ==//
    public void updatePassword(PasswordEncoder passwordEncoder, String password) {
        this.password = passwordEncoder.encode(password);
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }

    //== 패스워드 암호화 ==//
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    //비밀번호 변경, 회원 탈퇴 시, 비밀번호를 확인하며, 이때 비밀번호의 일치여부를 판단하는 메서드입니다.
    public boolean matchPassword(PasswordEncoder passwordEncoder, String checkPassword){
        return passwordEncoder.matches(checkPassword, getPassword());
    }

    //회원가입시, USER의 권한을 부여하는 메서드입니다.
    public void addUserAuthority() {
        this.logInRole = LogInRole.USER;
    }

//    private static final Map<MemberRole, String> roleToTextMap = Map.of(
//            MemberRole.FATHER, "아빠",
//            MemberRole.MOTHER, "엄마",
//            MemberRole.SON, "아들",
//            MemberRole.DAUGHTER, "딸");
//
//    private static final Map<String, MemberRole> textToRoleMap = roleToTextMap.entrySet().stream()
//            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//
//    public String getRole() {
//        return roleToTextMap.getOrDefault(this.role, "미정");
//    }
//
//    public void setRole(String role) {
//        this.role = textToRoleMap.getOrDefault(role, MemberRole.UNSPECIFIED);
//    }
}
