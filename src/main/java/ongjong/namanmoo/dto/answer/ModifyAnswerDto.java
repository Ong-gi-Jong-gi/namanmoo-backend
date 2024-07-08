package ongjong.namanmoo.dto.answer;

import lombok.Data;
import ongjong.namanmoo.domain.answer.Answer;


@Data
public class ModifyAnswerDto {
    private String answer;

    public ModifyAnswerDto(Answer newAnswer) {
        this.answer = newAnswer.getAnswerContent();
    }
}
