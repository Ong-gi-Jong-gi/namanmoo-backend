package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.Lucky;

import java.util.List;

@Entity
@Getter @Setter
public class Challenge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

//    @ManyToOne
//    @JoinColumn(name = "lucky_id")
//    private Lucky lucky;

    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
    private ChallengeType challengeType;        //enum 타입

    private String challengeTitle;

    //    @Column(nullable = false)
    private Long challengeNum;

    @OneToMany(mappedBy = "challenge")
    private List<Answer> answers;


//    public abstract ChallengeType getChallengeType();
    // Getters and Setters
}
