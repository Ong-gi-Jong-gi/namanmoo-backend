package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("P")
public class Photo extends Challenge {
    private String photoChallenge;
}
