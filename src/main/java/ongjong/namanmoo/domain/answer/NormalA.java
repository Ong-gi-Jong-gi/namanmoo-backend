package ongjong.namanmoo.domain.answer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@DiscriminatorValue("N")
@Entity
@Getter
@Setter
public class NormalA extends Answer{
    private String normalAnswer;
}
