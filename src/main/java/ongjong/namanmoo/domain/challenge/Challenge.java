package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.*;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.Lucky;

import java.util.List;

@Entity
@DiscriminatorColumn(name = "ctype")
public abstract class Challenge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

    @Column(nullable = false)
    private Long challengeNum;

    @OneToMany(mappedBy = "challenge")
    private List<Answer> answers;

    // Getters and Setters
}
