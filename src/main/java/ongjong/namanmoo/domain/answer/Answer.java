package ongjong.namanmoo.domain.answer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.Challenge;


@Entity
@Getter @Setter
@RequiredArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private AnswerType answerType;      // enum 타입

    private boolean bubbleVisible;

    private String createDate;

    private String modifiedDate;

    private String answerContent;
}
