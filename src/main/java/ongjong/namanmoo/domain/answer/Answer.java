package ongjong.namanmoo.domain.answer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.Challenge;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    //    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnswerType answerType;      // enum 타입

    private boolean checkChallenge = false;

//    @Column(nullable = false)
    private String createDate;

    //    @Column(nullable = false)
    private String modifiedDate;

    private String answerContent;
}

