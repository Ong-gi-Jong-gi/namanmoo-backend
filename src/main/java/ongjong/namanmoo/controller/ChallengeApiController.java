package ongjong.namanmoo.controller;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.ChallengeDto;
import ongjong.namanmoo.response.BaseResponse;
import ongjong.namanmoo.service.ChallengeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChallengeApiController {

    private final ChallengeService challengeService;

    @GetMapping("/challenge/today")     // 오늘의 챌린지 조회
    public ResponseEntity<BaseResponse> getChallenge(@RequestBody ChallengeRequest request) {
        Long familyId = request.getFamilyId();
        Challenge challenge = challengeService.findCurrentChallenge(familyId);
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BaseResponse("404", "Challenge not found", null));

        }
        ChallengeDto challengeDto = new ChallengeDto(challenge);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse("200", "Success", challengeDto));
    }

    @PostMapping("/challenge/create")       // 챌린지 생성
    public ResponseEntity<BaseResponse> saveChallenge(@RequestBody ChallengeRequest request){
        Long familyId = request.getFamilyId();
        if (!challengeService.join(familyId) || !challengeService.createAnswer(familyId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BaseResponse("404", "Challenge not found", null));

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse("200", "Success", null));
    }

    @Data
    static class ChallengeRequest {
        private Long familyId;
    }

}
