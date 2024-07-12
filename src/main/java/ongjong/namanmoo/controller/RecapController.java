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

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/recap")
@RequiredArgsConstructor
public class RecapController {

    private final LuckyService luckyService;
    private final AnswerService answerService;

    // 행운이 리스트
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


    // 리캡 컨텐츠 조회 - 통계
    @GetMapping("/statistics")
    public ApiResponse getStatistics(@RequestParam("luckyId") Long luckyId) {
        // 가장 조회수가 많은 챌린지
        Challenge mostViewedChallenge = challengeService.findMostViewedChallenge(luckyId);
        Map<String, Object> mostViewedData = new HashMap<>();
        if (mostViewedChallenge != null) {
            mostViewedData.put("topic", "가장 조회수가 많은 질문");
            mostViewedData.put("topicResult", mostViewedChallenge.getChallengeNum());
            mostViewedData.put("challengeTitle", mostViewedChallenge.getChallengeTitle());
            mostViewedData.put("challengeId", mostViewedChallenge.getChallengeId());
            mostViewedData.put("challengeType", mostViewedChallenge.getChallengeType());
        }
        // 모두가 가장 빨리 답한 챌린지
        Challenge fastestAnsweredChallenge = challengeService.findFastestAnsweredChallenge(luckyId);
        Map<String, Object> fastestAnsweredData = new HashMap<>();
        if (fastestAnsweredChallenge != null) {
            long fastestResponseTime = answerService.calculateFastestResponseTime(fastestAnsweredChallenge.getAnswers());
            fastestAnsweredData.put("topic", "모두가 가장 빨리 답한 질문");
            fastestAnsweredData.put("topicResult", fastestResponseTime);
            fastestAnsweredData.put("challengeTitle", fastestAnsweredChallenge.getChallengeTitle());
            fastestAnsweredData.put("challengeId", fastestAnsweredChallenge.getChallengeId());
            fastestAnsweredData.put("challengeType", fastestAnsweredChallenge.getChallengeType());
        }
        return new ApiResponse("200", "retrieved successfully", Arrays.asList(mostViewedData, fastestAnsweredData));
    }
}
