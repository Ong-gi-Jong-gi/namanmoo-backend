package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.sql.Time;


@Entity
@DiscriminatorValue("F")
public class FaceTime extends Challenge {
    private String faceTimeChallenge;
}

