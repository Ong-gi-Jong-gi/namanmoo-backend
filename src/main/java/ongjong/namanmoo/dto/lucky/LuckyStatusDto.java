package ongjong.namanmoo.dto.lucky;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LuckyStatusDto {
    private Integer status;
    @JsonProperty("isBubble")
    private boolean isBubble;
}
