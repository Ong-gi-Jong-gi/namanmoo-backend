package ongjong.namanmoo.controller;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.LuckyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@RestController
@RequestMapping("/lucky")
@RequiredArgsConstructor
public class LuckyController {
    private final LuckyService luckyService;

    @GetMapping
    public ApiResponse<LuckyStatusDto> getLuckyStatus(@RequestParam("challengeDate") String challengeDate) {
        LuckyStatusDto luckyStatusDto = luckyService.getLuckyStatus(challengeDate);
        return new ApiResponse<>("200", "Success", luckyStatusDto);
    }

}
