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

    @Enumerated(EnumType.STRING)
    private ChallengeType challengeType;        //enum 타입

    private String challengeTitle;

    private Long challengeNum;

    @OneToMany(mappedBy = "challenge")
    private List<Answer> answers;
}
