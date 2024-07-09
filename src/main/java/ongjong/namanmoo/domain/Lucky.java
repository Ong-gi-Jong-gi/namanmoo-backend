package ongjong.namanmoo.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.answer.FaceTimeAnswer;
import java.util.List;

@Entity
@Getter
@Setter
public class Lucky {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long luckyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Integer status;

    private String challengeStartDate;


    private boolean running;

    @OneToMany(mappedBy = "lucky")
    private List<FaceTimeAnswer> faceTimeAnswers;

}
