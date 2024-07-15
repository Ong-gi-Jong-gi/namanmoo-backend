package ongjong.namanmoo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.recap.MemberAndCountDto;
import ongjong.namanmoo.dto.recap.MemberPhotosAnswerDto;
import ongjong.namanmoo.dto.recap.MemberRankingListDto;
import ongjong.namanmoo.dto.recap.MemberYouthAnswerDto;
import ongjong.namanmoo.dto.recap.*;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.service.AnswerService;
import ongjong.namanmoo.service.ChallengeService;
import ongjong.namanmoo.service.LuckyService;
import ongjong.namanmoo.service.MemberService;
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
    private final ChallengeService challengeService;
    private final MemberService memberService;

    // 행운이 리스트
    @GetMapping("/list")
    public ResponseEntity<?> getLuckyList() {
        List<LuckyListDto> luckyListStatus = luckyService.getLuckyListStatus();
        return ResponseEntity.ok().body(new ApiResponse<>("200", "Success", luckyListStatus));
    }

    // recap 랭킹
    // facetime 테이블 지우면 정상적으로 작동
    @GetMapping("/ranking")
    public ApiResponse<MemberRankingListDto> getRanking(@RequestParam("luckyId") Long luckyId){
        Lucky lucky = luckyService.getLucky(luckyId);
        Integer totalCount = lucky.getStatus();
        Integer luckyStatus = luckyService.calculateLuckyStatus(lucky);
        List<MemberAndCountDto> memberAndCountList = memberService.getMemberAndCount(lucky);
        MemberRankingListDto responseDto = new MemberRankingListDto(totalCount,luckyStatus,memberAndCountList);
        return new ApiResponse<>("200", "Ranking retrieved successfully", responseDto);
    }

    // recap 화상통화
    @GetMapping("/face")
    public ApiResponse<MemberFacetimeDto> getFacetime(@RequestParam("luckyId") Long luckyId){
        List<String> answerList = answerService.getFacetimeAnswerList(luckyId);
        MemberFacetimeDto facetimeAnswerList = new MemberFacetimeDto(answerList);
        return new ApiResponse<>("200", "Ranking retrieved successfully", facetimeAnswerList);
    }

    // 리캡 컨텐츠 조회 - 통계
    @GetMapping("/statistics")
    public ApiResponse<List<Map<String, Object>>> getStatistics(@RequestParam("luckyId") Long luckyId) throws Exception {
        Lucky lucky = luckyService.getLucky(luckyId);

        // 가장 조회수가 많은 챌린지
        Challenge mostViewedChallenge = challengeService.findMostViewedChallenge(lucky);
        Map<String, Object> mostViewedData = new HashMap<>();
        mostViewedData.put("topic", "mostViewed");
        mostViewedData.put("topicResult", mostViewedChallenge != null ? lucky.getChallengeViews().get(mostViewedChallenge.getChallengeNum()) : 0);
        mostViewedData.put("challengeId", mostViewedChallenge != null ? mostViewedChallenge.getChallengeId() : "");
        mostViewedData.put("challengeType", mostViewedChallenge != null ? mostViewedChallenge.getChallengeType() : "");
        mostViewedData.put("challengeNumber", mostViewedChallenge != null ? mostViewedChallenge.getChallengeNum() : "");
        mostViewedData.put("challengeTitle", mostViewedChallenge != null ? mostViewedChallenge.getChallengeTitle() : "");

        // 모두가 가장 빨리 답한 챌린지
        Challenge fastestAnsweredChallenge = challengeService.findFastestAnsweredChallenge(lucky);
        Map<String, Object> fastestAnsweredData = new HashMap<>();
        fastestAnsweredData.put("topic", "fastestAnswered");
        fastestAnsweredData.put("topicResult", fastestAnsweredChallenge != null ? challengeService.calculateLatestResponseTime(lucky, fastestAnsweredChallenge) : 0);
        fastestAnsweredData.put("challengeId", fastestAnsweredChallenge != null ? fastestAnsweredChallenge.getChallengeId() : "");
        fastestAnsweredData.put("challengeType", fastestAnsweredChallenge != null ? fastestAnsweredChallenge.getChallengeType() : "");
        fastestAnsweredData.put("challengeNumber", fastestAnsweredChallenge != null ? fastestAnsweredChallenge.getChallengeNum() : "");
        fastestAnsweredData.put("challengeTitle", fastestAnsweredChallenge != null ? fastestAnsweredChallenge.getChallengeTitle() : "");

        return new ApiResponse<>("200", "retrieved successfully", Arrays.asList(mostViewedData, fastestAnsweredData));
    }

    // recap 과거 사진
    // challengeNum => 13: 나의 어렸을 때 장래희망
    // cahllengeNum => 28 : 자신의 어렸을 적 사진 ( 23 : 학생 때 졸업사진 , 9: 가장 마음에 드는 본인 사진)
    // TODO: 질문 데이터 set 수정 후, ChallengeNum 수정 필요
    @GetMapping("/youth")
    public ApiResponse<List<MemberYouthAnswerDto>> getYouth(@RequestParam("luckyId") Long luckyId) throws Exception{
        List<Member> members = memberService.getMembersByLuckyId(luckyId);
        List<MemberYouthAnswerDto> memberAnswerDtoList = answerService.getYouthByMember(members, 1, 9);
        return new ApiResponse<>("200", "Youth photos retrieved successfully", memberAnswerDtoList);
    }

    // TODO: 질문 데이터 set 수정 후, ChallengeNum 수정 필요
    // recap 미안한점 고마운점
    @GetMapping("/appreciations")
    public ApiResponse<List<MemberAppreciationDto>> getAppreciations(@RequestParam("luckyId") Long luckyId) throws Exception {
        List<Member> members = memberService.getMembersByLuckyId(luckyId);
        List<MemberAppreciationDto> appreciationList = answerService.getAppreciationByMember(members, 2, 3);
        return new ApiResponse<>("200", "Success", appreciationList);
    }


    // recap 가족사진
    @GetMapping("/photos")
    public ApiResponse<MemberPhotosAnswerDto> getPhotos(@RequestParam("luckyId") Long luckyId) throws Exception{
        List<Member> members = memberService.getMembersByLuckyId(luckyId);
        MemberPhotosAnswerDto photosAnswerDto = answerService.getPhotoByMember(members);
        return new ApiResponse<>("200", "Success", photosAnswerDto);
    }

}
