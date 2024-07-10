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

//    // recap 랭킹
//    @GetMapping("/ranking")
//    public ApiResponse getChallenge(@RequestParam("luckyId") Long luckyId) throws Exception {
//        // total 카운트는 현재 가족이 챌린지에 참여한 횟수(가족의 맴버 한명이 참여하면 +1 )
//    }

}
