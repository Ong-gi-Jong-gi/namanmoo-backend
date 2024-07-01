package ongjong.namanmoo.domain.answer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import ongjong.namanmoo.domain.challenge.Challenge;

@Entity
@DiscriminatorValue("G")
public class GroupA extends Answer {
    private String groupAnswer;
}
