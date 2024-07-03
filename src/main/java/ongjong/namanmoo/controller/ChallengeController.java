package ongjong.namanmoo.controller;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.challenge.ChallengeDto;
import ongjong.namanmoo.dto.challenge.ChallengeListDto;
import ongjong.namanmoo.repository.MemberRepository;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.ChallengeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenge")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final MemberRepository memberRepository;

    @PostMapping("/create")       // 챌린지 생성 -> 캐릭터 생성 및 답변 생성
    public ResponseEntity<ApiResponse> saveChallenge(@RequestBody ChallengeRequest request){
        Long familyId = request.getFamilyId();
        if (!challengeService.join(familyId) || !challengeService.createAnswer(familyId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("404", "Challenge not found", null));

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("200", "Success", null));
    }

    @GetMapping("/today")     // 오늘의 챌린지 조회
    public ResponseEntity<ApiResponse> getChallenge(@RequestBody ChallengeRequest request) {
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
    public ResponseEntity<ApiResponse<List<ChallengeListDto>>> getChallengeList(@RequestBody ChallengeRequest request) {
        Long familyId = request.getFamilyId();
        List<Challenge> challenges = challengeService.findChallenges(familyId);

        if (challenges == null || challenges.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("failure", "No challenges found for the family", null));
        }

        List<Member> members = memberRepository.findByFamilyId(familyId);
        Member member = members.get(0);     // 일단 첫번째 맴버로 TODO : 로그인 한 사람으로 바꿔야함
        List<ChallengeListDto> challengeList = challenges.stream()
                .map(challenge -> {
                    boolean isComplete = challengeService.findIsCompleteAnswer(challenge, member);
                    return new ChallengeListDto(challenge, isComplete);
                })
                .collect(Collectors.toList());


        return ResponseEntity.ok(new ApiResponse<>("success", "Challenge list retrieved successfully", challengeList));
    }

    @Data
    static class ChallengeRequest {
        private Long familyId;
    }

}
