package ongjong.namanmoo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.recapMember.MemberAndCountDto;
import ongjong.namanmoo.dto.recapMember.MemberRankingListDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.AnswerService;
import ongjong.namanmoo.service.LuckyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recap")
@RequiredArgsConstructor
public class RecapController {

    private final LuckyService luckyService;
    private final AnswerService answerService;

    @GetMapping("/list")
    public ResponseEntity<?> getLuckyList() {
        List<LuckyListDto> luckyListStatus = luckyService.getLuckyListStatus();
        return ResponseEntity.ok().body(new ApiResponse<>("200", "Success", luckyListStatus));
    }

    // recap 랭킹
    @GetMapping("/ranking")
    public ApiResponse getRanking(@RequestParam("luckyId") Long luckyId) throws Exception {
         // getMemberCount() 함수 사용
        Lucky lucky = luckyService.getLucky(luckyId);
        Integer totalCount = lucky.getStatus();
        Integer luckyStatus = luckyService.calculateLuckyStatus(lucky);
        List<MemberAndCountDto> memberAndCountList = answerService.getMemberAndCount(lucky);
        MemberRankingListDto responseDto = new MemberRankingListDto(totalCount,luckyStatus,memberAndCountList);
        return new ApiResponse<>("success", "Ranking retrieved successfully", responseDto);
    }

}
