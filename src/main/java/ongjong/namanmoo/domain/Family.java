package ongjong.namanmoo.domain;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyId;

    private String familyName;

    @Column(nullable = false)
    private Long maxFamilySize;

    @Column(nullable = false, columnDefinition = "bigint default 1")
    private Long currentFamilySize;

    @Column(nullable = false, unique = true)
    private String inviteCode;

    @Column(columnDefinition = "bigint default 0")
    private Long challengeFamilyCount;

    private Timestamp challengeStartDate;

    private Long familyOwnerId;

    @OneToMany(mappedBy = "family")
    private List<User> users;

    @OneToMany(mappedBy = "family")
    private List<Object> objects;

    // Getters and Setters
}
