package ongjong.namanmoo.controller;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.challenge.ChallengeDto;
import ongjong.namanmoo.dto.challenge.ChallengeListDto;
import ongjong.namanmoo.dto.challenge.NormalChallengeDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.AnswerRepository;
import ongjong.namanmoo.repository.ChallengeRepository;
import ongjong.namanmoo.repository.MemberRepository;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.ChallengeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenge")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final MemberRepository memberRepository;
    private final AnswerRepository answerRepository;
    private final ChallengeRepository challengeRepository;

    @PostMapping("/create")       // 챌린지 생성 -> 캐릭터 생성 및 답변 생성
    public ResponseEntity<ApiResponse> saveChallenge(@RequestBody FamilyIdRequest request){
        Long familyId = request.getFamilyId();
        if (!challengeService.join(familyId) || !challengeService.createAnswer(familyId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("404", "Challenge not found", null));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("200", "Success", null));
    }

    @GetMapping("/today")     // 오늘의 챌린지 조회
    public ResponseEntity<ApiResponse> getChallenge(@RequestBody FamilyIdRequest request) {
        Long familyId = request.getFamilyId();
        Challenge challenge = challengeService.findCurrentChallenge(familyId);
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("404", "Challenge not found", null));

        }
        ChallengeDto challengeDto = new ChallengeDto(challenge);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("200", "Success", challengeDto));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ChallengeListDto>>> getChallengeList(@RequestBody FamilyIdRequest request) {
        Long familyId = request.getFamilyId();
        List<Challenge> challenges = challengeService.findChallenges(familyId);

        if (challenges == null || challenges.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("failure", "No challenges found for the family", null));
        }

        Optional<Member> member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()); // 로그인한 멤버 찾기
        List<ChallengeListDto> challengeList = challenges.stream()
                .map(challenge -> {
                    boolean isComplete = challengeService.findIsCompleteAnswer(challenge, member.orElse(null));
                    return new ChallengeListDto(challenge, isComplete);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>("success", "Challenge list retrieved successfully", challengeList));
    }

    @GetMapping("/normal")
    public ResponseEntity<ApiResponse<List<NormalChallengeDto>>> getNormalChallenge(@RequestBody ChallengeIdRequest request) {
        Long challengeId = request.getChallengeId();
        Challenge challenge = challengeRepository.findById(challengeId).get();
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null));
        }

        Optional<Member> member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()); // 로그인한 멤버 찾기

        boolean isComplete = challengeService.findIsCompleteAnswer(challenge, member.orElse(null));
        List<Answer> answers = answerRepository.findByChallenge(challenge);

        NormalChallengeDto NormalChallengeDto = new NormalChallengeDto(challenge, isComplete, answers);

        return ResponseEntity.ok(new ApiResponse<>("success", "Challenge retrieved successfully", Collections.singletonList(NormalChallengeDto)));
    }


    @Data
    static class FamilyIdRequest {
        private Long familyId;
    }

    @Data
    static class ChallengeIdRequest {
        private Long challengeId;
    }


}
