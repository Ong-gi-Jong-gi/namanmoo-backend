package ongjong.namanmoo.domain.challenge;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("V")
public class Voice extends Challenge {
    private String voiceChallenge;
}
