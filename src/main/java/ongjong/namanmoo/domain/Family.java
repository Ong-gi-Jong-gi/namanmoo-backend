package ongjong.namanmoo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Getter @Setter
@Entity
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyId;

    private String familyName;

    private int maxFamilySize = 4; // 가족 최대 인원 수, 기본값 4

    private int currentFamilySize;

    private String inviteCode;

    @Column(columnDefinition = "bigint default 0")
    private Long challengeFamilyCount;

    private Long familyOwnerId;

    @OneToMany(mappedBy = "family")
    private List<Member> members;

    @OneToMany(mappedBy = "family")
    private List<Lucky> luckies;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final Random RANDOM = new SecureRandom();

    // 초대 코드 생성 메서드 (SecureRandom 사용)
    public void generateInviteCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        this.inviteCode = code.toString();
    }
//    // 초대 코드 생성 메서드 (UUID 사용)
//    public void generateInviteCode() {
//        this.inviteCode = UUID.randomUUID().toString().substring(0, CODE_LENGTH);
//    }
}