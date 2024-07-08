package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.challenge.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
     List<Challenge> findByChallengeNum(Long challengeNum);            // 그룹 챌린지일 경우 같은 challengenum을 같는 챌린지는 두개가 존재한다.
     List<Challenge> findByChallengeNumBetween(Long startChallengeNum, Long challengeNum);        // challengeNum 이하의 challenge 모두 반환
}
