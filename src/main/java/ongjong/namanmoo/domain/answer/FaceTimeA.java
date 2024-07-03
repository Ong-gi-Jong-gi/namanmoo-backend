package ongjong.namanmoo.domain.answer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("F")
public class FaceTimeA extends Answer {
    private String facePhoto1;
    private String facePhoto2;
    private String facePhoto3;
    private String facePhoto4;
}

