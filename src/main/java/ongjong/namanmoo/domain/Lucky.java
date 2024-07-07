package ongjong.namanmoo.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.answer.FaceTimeAnswer;
import ongjong.namanmoo.domain.challenge.Challenge;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
public class Lucky {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long luckyId;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long status;

    private String challengeStartDate;

//    private Long currentChallengeNum;

    private boolean running;

    @OneToMany(mappedBy = "lucky")
    private List<FaceTimeAnswer> faceTimeAnswers;

//    @OneToMany(mappedBy = "lucky")
//    private List<Challenge> challenges;

    // Getters and Setters
}
