package ongjong.namanmoo.domain;


import jakarta.persistence.*;
import lombok.*;
import ongjong.namanmoo.domain.answer.FaceTimeAnswer;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lucky {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long luckyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer status;

    private String challengeStartDate;

    private boolean running;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ChallengeLength lifetime = ChallengeLength.THIRTY_DAYS; // 행운이 기본 수명 ( 30일 단위 챌린지 )

    @OneToMany(mappedBy = "lucky")
    private List<FaceTimeAnswer> faceTimeAnswers;
}