package ongjong.namanmoo.domain.challenge;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("N")
@Getter
@Setter
public class NormalC extends Challenge {
    private String normalChallenge;
}
