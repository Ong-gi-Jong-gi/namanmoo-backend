package ongjong.namanmoo.domain.challenge;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("N")
public class NormalC extends Challenge {
    private String normalChallenge;
}
