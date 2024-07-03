package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


@Entity
@DiscriminatorValue("F")
public class FaceTimeC extends Challenge {
    private String faceTimeChallenge;
}

