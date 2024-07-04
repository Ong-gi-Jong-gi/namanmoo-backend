package ongjong.namanmoo.domain;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static lombok.AccessLevel.PROTECTED;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(nullable = false, unique = true)
    private String loginId; // 아이디

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String name; // 이름

    @Builder.Default
    private String role = "미정"; // 가족에서의 역할

    private String nickname; // 별명

    @Builder.Default
    private boolean checkChallenge = false;

    private Long challengeMemberCount;

    @Builder.Default
    private String memberImage = "https://~"; // 프로필 사진

    @Enumerated(EnumType.STRING)
    private LogInRole logInRole; // 권한 -> USER, ADMIN


    //== 정보 수정 ==//
    public void updatePassword(PasswordEncoder passwordEncoder, String password) {
        this.password = passwordEncoder.encode(password);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRole(String role) {
        this.role = role;
    }

    //== jwt 토큰 추가 ==//
    @Column(length = 1000)
    private String refreshToken;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }

//    // 아직 정확히 모름
//    public void updateMemberImage(String memberImage) {
//        this.memberImage = memberImage;
//    }

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
}
