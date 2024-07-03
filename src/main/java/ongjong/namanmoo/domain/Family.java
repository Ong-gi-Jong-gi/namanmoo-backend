package ongjong.namanmoo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String familyName;

    @Column(nullable = false)
    @Setter
    private Long maxFamilySize;

    @Column(nullable = false, columnDefinition = "bigint default 1")
    @Setter
    private Long currentFamilySize;

    @Column(nullable = false, unique = true)
    @Setter
    private String inviteCode;

    @Column(columnDefinition = "bigint default 0")
    @Setter
    private Long challengeFamilyCount;

    @Setter
    private Long familyOwnerId;

    @OneToMany(mappedBy = "family")
    private List<Member> members;

    @OneToMany(mappedBy = "family")
    private List<Lucky> luckies;

    // Getters and Setters
}
