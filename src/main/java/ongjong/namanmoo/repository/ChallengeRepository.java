package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.ChallengeDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
     Challenge findByChallengeNum(Long challengeNum);
}
