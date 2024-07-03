package ongjong.namanmoo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
public class VoiceMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voiceId;

    @ManyToOne
    @JoinColumn(name = "memeber_id")
    private Member member;

    @Column(nullable = false)
    private String voiceFile;

    @Setter
    private String voiceTitle;

    private Long sender;

    private Long receiver;

    @Setter
    private Timestamp date;

    private Long voiceLength;

    @Setter
    private boolean checkRead = false;

    // Getters and Setters
}

