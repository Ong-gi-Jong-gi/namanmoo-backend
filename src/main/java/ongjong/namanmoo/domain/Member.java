package ongjong.namanmoo.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

//    @Column(nullable = false, unique = true)
    @Setter
    private String loginId;

    @Setter
//    @Column(nullable = false)
    private String password;

    @Setter
//    @Column(nullable = false)
    private String name;

    @Setter
//    @Column(nullable = false)
    private String role;

    @Setter
    private String nickname;

    @Setter
//    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long challengeMemberCount;

    @Setter
//    @Column(nullable = false)
    private boolean checkChallenge = false;

    @Setter
    private String memberImage;

    // Getters and Setters
}


