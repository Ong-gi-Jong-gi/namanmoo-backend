package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@Entity
@DiscriminatorValue("F")
@Getter
@Setter
public class FaceTimeC extends Challenge {
    private String faceTimeChallenge;
}

