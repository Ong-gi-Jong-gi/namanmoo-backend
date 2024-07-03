package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.challenge.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

}
