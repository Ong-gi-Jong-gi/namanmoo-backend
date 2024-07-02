package ongjong.namanmoo.domain;


import jakarta.persistence.*;
import ongjong.namanmoo.domain.challenge.Challenge;

import java.util.List;

@Entity
public class Object {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long objectId;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long status;

    @OneToMany(mappedBy = "object")
    private List<Challenge> challenges;

    // Getters and Setters
}
