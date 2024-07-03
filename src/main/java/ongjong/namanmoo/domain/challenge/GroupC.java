package ongjong.namanmoo.domain.challenge;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("G")
@Getter
@Setter
public class GroupC extends Challenge{
    private String parentChallenge;
    private String childChallenge;
}
