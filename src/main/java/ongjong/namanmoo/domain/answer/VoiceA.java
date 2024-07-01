package ongjong.namanmoo.domain.answer;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import ongjong.namanmoo.domain.challenge.Challenge;

@Entity
@DiscriminatorValue("V")
public class VoiceA extends Answer {
    private String voiceFile;
}
