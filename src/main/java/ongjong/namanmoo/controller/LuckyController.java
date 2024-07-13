package ongjong.namanmoo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.dto.lucky.OffBubbleDto;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.service.AnswerService;
import ongjong.namanmoo.service.LuckyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lucky")
@RequiredArgsConstructor
public class LuckyController {
    private final LuckyService luckyService;
    private final AnswerService answerService;

    @GetMapping
    public ApiResponse<LuckyStatusDto> getLuckyStatus(@RequestParam("challengeDate") String challengeDate) {
        LuckyStatusDto luckyStatusDto = luckyService.getLuckyStatus(challengeDate);
        return new ApiResponse<>("200", "Success", luckyStatusDto);
    }

    @PostMapping("/bubble")
    public ApiResponse<Void> updateBubble(@Valid @RequestBody OffBubbleDto offBubbleDto) throws Exception {
        answerService.offBalloon(offBubbleDto.getChallengeDate());
        return new ApiResponse<>("200", "Lucky Bubble Off", null);
    }

}
