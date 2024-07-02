package ongjong.namanmoo.domain;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class VoiceMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voiceId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String voiceFile;

    private String voiceTitle;

    private Long sender;

    private Long receiver;

    private Timestamp date;

    private Long voiceLength;

    private boolean checkRead = false;

    // Getters and Setters
}

