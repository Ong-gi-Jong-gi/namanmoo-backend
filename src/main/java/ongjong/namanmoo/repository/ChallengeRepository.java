package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.challenge.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long > {
     List<Challenge> findByChallengeNum(Integer challengeNum);            // 그룹 챌린지일 경우 같은 challengenum을 같는 챌린지는 두개가 존재한다.
     List<Challenge> findByChallengeNumBetween(Integer startChallengeNum, Integer challengeNum);        // challengeNum 이하의 challenge 모두 반환

     Challenge findTopByLuckyIdOrderByChallengeNumDesc(Long luckyId);
     List<Challenge> findAllByLuckyId(Long luckyId);
}
