package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("G")
public class GroupC extends Challenge{
    private String parentChallenge;
    private String childChallenge;
}
