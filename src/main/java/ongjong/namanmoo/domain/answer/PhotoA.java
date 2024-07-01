package ongjong.namanmoo.domain.answer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import ongjong.namanmoo.domain.challenge.Challenge;

@Entity
@DiscriminatorValue("P")
public class PhotoA extends Answer {
    private String photoFile;
}
