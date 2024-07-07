package ongjong.namanmoo.controller;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.challenge.ChallengeDto;
import ongjong.namanmoo.dto.challenge.ChallengeListDto;
import ongjong.namanmoo.dto.challenge.NormalChallengeDto;
import ongjong.namanmoo.repository.AnswerRepository;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final MemberService memberService;
    private final LuckyService luckyService;
    private final AnswerService answerService;
    private final FamilyService familyService;
    private final AnswerRepository answerRepository;

    @PostMapping("/")       // 챌린지 생성 -> 캐릭터 생성 및 답변 생성
    public ResponseEntity<ApiResponse> saveChallenge() throws Exception {
        Long familyId = familyService.findFamilyId();
        if (!luckyService.join(familyId) || !answerService.createAnswer(familyId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("404", "Challenge not found", null));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("200", "Success", null));
    }

    @GetMapping("/today")     // 오늘의 챌린지 조회
    public ResponseEntity<ApiResponse> getChallenge(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        List<Challenge> challenges = challengeService.findChallengeByMemberId(challengeDate);
        Challenge challenge = challengeService.findCurrentChallenge(challenges);
        answerService.saveCreateDate(challenge);    // answer에 createdate 저장
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("404", "Challenge not found", null));

        }
        Long currentNum  = challengeService.findCurrentNum(challengeDate);
        ChallengeDto challengeDto = new ChallengeDto(challenge,currentNum);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("200", "Success", challengeDto));
    }

    @GetMapping("/list")        // 챌린지 리스트 조회 , 챌린지 리스트는 lucky가 여러개 일때를 고려하여 죽은 럭키 개수 * 30 +1 부터 챌린지가 보여져야한다.
    public ResponseEntity<ApiResponse<List<ChallengeListDto>>> getChallengeList(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        List<Challenge> challenges = challengeService.findChallenges(challengeDate); //

        if (challenges == null || challenges.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("failure", "No challenges found for the family", null));
        }

        Member member = challengeService.findMemberByLoginId(); // 로그인한 멤버 찾기
        List<ChallengeListDto> challengeList = challenges.stream()
                .map(challenge -> {
                    boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
                    return new ChallengeListDto(challenge, isComplete);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>("success", "Challenge list retrieved successfully", challengeList));
    }

    @GetMapping("/normal")      // 일반 챌린지 조회
    public ResponseEntity<ApiResponse<List<NormalChallengeDto>>> getNormalChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null));
        }

        Member member = challengeService.findMemberByLoginId(); // 로그인한 멤버 찾기

        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        Long timeStamp= answerService.findAnswerByChallengeMember(challenge,member);
        List<Answer> answers = answerService.findAnswerByChallenge(challenge);

        NormalChallengeDto NormalChallengeDto = new NormalChallengeDto(challenge, isComplete, timeStamp,answers);

        return ResponseEntity.ok(new ApiResponse<>("success", "Challenge retrieved successfully", Collections.singletonList(NormalChallengeDto)));      // 객체를 리스트 형태로 감싸서 반환
    }

//    @PostMapping("/normal")     // 일반 챌린지 내용 수정
//    public  ResponseEntity<ApiResponse> saveAnswer(@RequestBody AnswerRequest request){
//        Long challengeId = request.getChallengeId();
//        String answer = request.getAnswer();
//
//    }


    @Data
    static class AnswerRequest {
        private Long challengeId;
        private String answer;
    }



}
