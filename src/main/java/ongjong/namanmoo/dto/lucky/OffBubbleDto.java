package ongjong.namanmoo.dto.lucky;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffBubbleDto {
    @JsonProperty("challengeDate")
    private Long challengeDate;
}
