package ongjong.namanmoo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.recapMember.MemberAndCountDto;
import ongjong.namanmoo.dto.recapMember.MemberRankingListDto;
import ongjong.namanmoo.dto.recapMember.MemberYouthAnswerDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.AnswerService;
import ongjong.namanmoo.service.LuckyService;
import ongjong.namanmoo.service.MemberService;
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

    // recap 과거 사진
    // challengeNum => 13: 나의 어렸을 때 장래희망
    // cahllengeNum => 28 : 자신의 어렸을 적 사진 ( 23 : 학생 때 졸업사진 , 9: 가장 마음에 드는 본인 사진)
    @GetMapping("/youth")
    public ApiResponse<List<MemberYouthAnswerDto>> getYouth(@RequestParam("luckyId") Long luckyId) throws Exception{
        List<Member> members = memberService.getMembersByLuckyId(luckyId);
        List<MemberYouthAnswerDto> memberAnswerDtoList = answerService.getAnswerByMember(members);
        return new ApiResponse<>("200", "Youth photos retrieved successfully", memberAnswerDtoList);
    }

//    // recap 가족사진
//    @GetMapping("/photos")
//    public ApiResponse getPhotos(@RequestParam("luckyId") Long luckyId) throws Exception{
//        List<Member> members = memberService.getMembersByLuckyId(luckyId);
//
//    }


}
