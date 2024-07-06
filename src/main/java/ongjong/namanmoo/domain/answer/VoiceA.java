package ongjong.namanmoo.domain.answer;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.challenge.Challenge;

@Entity
@DiscriminatorValue("V")
@Getter
@Setter
public class VoiceA extends Answer {
    private String voiceFile;
}
