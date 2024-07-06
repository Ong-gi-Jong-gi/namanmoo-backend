package ongjong.namanmoo.domain.answer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import ongjong.namanmoo.domain.challenge.Challenge;

@Entity
@DiscriminatorValue("G")
@Getter
@Setter
public class GroupA extends Answer {
    private String groupAnswer;
}
