package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByChallengeAndMember(Challenge challenge, Member member);
    List<Answer> findByChallenge(Challenge challenge);
    Optional<Answer> findByMemberAndCreateDate(Member member, String createDate);
    boolean existsByMemberAndCreateDateAndAnswerContentIsNotNull(Member member, String createDate);
    List<Answer> findByChallengeAndMemberFamilyFamilyId(Challenge challenge, Long familyId);
}
