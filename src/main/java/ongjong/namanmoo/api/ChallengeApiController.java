package ongjong.namanmoo.api;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.repository.ChallengeDto;
import ongjong.namanmoo.service.ChallengeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChallengeApiController {

    private final ChallengeService challengeService;

    @GetMapping("/challenge/today")     // 오늘의 챌린지 조회
    public ChallengeDto getChallenge(@RequestBody ChallengeRequest request) {
        Long familyId = request.getFamilyId();
        Challenge challenge = challengeService.findCurrentChallenge(familyId);
        return new ChallengeDto(challenge);
    }

    @Data
    static class ChallengeRequest {
        private Long familyId;
    }
}
