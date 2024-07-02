package ongjong.namanmoo.domain.answer;

import jakarta.persistence.*;
import ongjong.namanmoo.domain.User;
import ongjong.namanmoo.domain.challenge.Challenge;

import java.sql.Timestamp;

@Entity
@DiscriminatorColumn(name = "atype")
public abstract class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Timestamp createDate;

    @Column(nullable = false)
    private Timestamp modifiedDate;

    // Getters and Setters
}

