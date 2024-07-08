package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.challenge.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
     Challenge findByChallengeNum(Long challengeNum);
     List<Challenge> findByChallengeNumLessThanEqual(Long challengeNum);        // challengeNum 이하의 challenge 모두 반환
}
