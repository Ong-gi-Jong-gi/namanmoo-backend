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

    //    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnswerType answerType;      // enum 타입

    private boolean bubbleVisible;

//    @Column(nullable = false)
    private String createDate;

    //    @Column(nullable = false)
    private String modifiedDate;

    private String answerContent;
}
