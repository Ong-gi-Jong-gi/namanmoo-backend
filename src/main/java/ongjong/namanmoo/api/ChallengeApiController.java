package ongjong.namanmoo.api;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.service.ChallengeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChallengeApiController {

    private final ChallengeService challengeService;

    @PostMapping("/challenge/today")
    public Challenge getChallenge(@RequestBody ChallengeRequest request) {
        Long familyId = request.getFamilyId();
        Challenge challenge = challengeService.findCurrentChallenge(familyId);
        return new ChallengeResponse(challenge).getChallenge();
    }


    @Data
    static class ChallengeRequest {
        private Long familyId;
    }
    @Data
    static class ChallengeResponse {
        private final Challenge challenge;
        public ChallengeResponse(Challenge challenge) {
            this.challenge = challenge;
        }
    }
}
