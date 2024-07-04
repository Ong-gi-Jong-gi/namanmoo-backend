package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("P")
@Getter
@Setter
public class PhotoC extends Challenge {
    private String photoChallenge;
}
