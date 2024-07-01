package ongjong.namanmoo.domain.answer;

import jakarta.persistence.DiscriminatorValue;

@DiscriminatorValue("N")
public class NormalA extends Answer{
    private String normalAnswer;
}
