package ongjong.namanmoo.domain.challenge;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("V")
@Getter
@Setter
public class VoiceC extends Challenge {
    private String voiceChallenge;
}
