package ongjong.namanmoo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.dto.lucky.OffBubbleDto;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lucky")
@RequiredArgsConstructor
public class LuckyController {
    private final LuckyService luckyService;
    private final AnswerService answerService;

    @GetMapping
    public ApiResponse<LuckyStatusDto> getLuckyStatus() throws Exception {
        Long challengeDate = System.currentTimeMillis();
        luckyService.luckyDeadOrAlive(challengeDate);
        LuckyStatusDto luckyStatusDto = luckyService.getLuckyStatus(challengeDate);
        return new ApiResponse<>("200", "Success", luckyStatusDto);
    }

    @PostMapping("/bubble")
    public ApiResponse<Void> updateBubble(@Valid @RequestBody OffBubbleDto offBubbleDto) throws Exception {
        answerService.offBalloon(offBubbleDto.getChallengeDate());
        return new ApiResponse<>("200", "Lucky Bubble Off", null);
    }


}
