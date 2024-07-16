package ongjong.namanmoo.dto.lucky;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LuckyListDto {
    private String luckyId;
    private Long startDate;
    private Long endDate;
    private Integer luckyStatus;
    private boolean running;
}