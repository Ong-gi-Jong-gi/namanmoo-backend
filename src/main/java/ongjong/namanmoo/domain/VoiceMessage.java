package ongjong.namanmoo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class VoiceMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voiceId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

//    @Column(nullable = false)
    private String voiceFile;

    private String voiceTitle;

    private Long sender;

    private Long receiver;

    private Timestamp date;

    private Long voiceLength;

    private boolean checkRead = false;

    // Getters and Setters
}

