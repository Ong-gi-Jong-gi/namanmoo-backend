package ongjong.namanmoo.domain.answer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@Entity
@DiscriminatorValue("F")
@Getter
@Setter
public class FaceTimeA extends Answer {
    private String facePhoto1;
    private String facePhoto2;
    private String facePhoto3;
    private String facePhoto4;
}

