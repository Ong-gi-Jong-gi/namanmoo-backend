package ongjong.namanmoo.domain.answer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.Lucky;

@Entity
@Getter @Setter
public class FaceTimeAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long faceTimeAnswerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lucky_id")
    private Lucky lucky;

    private String faceTimeAnswer1;
    private String faceTimeAnswer2;
    private String faceTimeAnswer3;
    private String faceTimeAnswer4;

}
