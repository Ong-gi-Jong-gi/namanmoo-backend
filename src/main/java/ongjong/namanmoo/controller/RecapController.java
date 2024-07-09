package ongjong.namanmoo.controller;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.challenge.ChallengeDto;
import ongjong.namanmoo.global.security.util.DateUtil;
import ongjong.namanmoo.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recap")
public class RecapController {
//
//    @GetMapping("/ranking")     // 오늘의 챌린지 조회
//    public ApiResponse getChallenge(@RequestParam("luckyId") Long luckyId) throws Exception {
//
//    }

}
