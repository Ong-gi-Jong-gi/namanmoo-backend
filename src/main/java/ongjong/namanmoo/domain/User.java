package ongjong.namanmoo.domain;


import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private String nickname;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long challengeUserCount;

    @Column(nullable = false)
    private boolean checkChallenge = false;

    private String userImage;

    // Getters and Setters
}
