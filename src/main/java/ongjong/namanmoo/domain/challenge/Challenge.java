package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.Lucky;

import java.util.List;

@Entity
@DiscriminatorColumn(name = "ctype")
@Getter @Setter
public abstract class Challenge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

    @ManyToOne
    @JoinColumn(name = "lucky_id")
    private Lucky lucky;

    @Column(nullable = false)
    private Long challengeNum;

    @OneToMany(mappedBy = "challenge")
    private List<Answer> answers;

    // Getters and Setters
}
