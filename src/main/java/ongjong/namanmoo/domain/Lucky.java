package ongjong.namanmoo.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.challenge.Challenge;

import java.sql.Timestamp;
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

    private Integer status; // 성장 상태

    private Timestamp challengeStartDate; // 시작 날짜

    private boolean running; // 생존 여부
}
