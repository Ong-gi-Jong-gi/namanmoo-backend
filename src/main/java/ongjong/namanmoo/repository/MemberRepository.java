package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId (String loginId);

    Optional<Member> findByRefreshToken(String refreshToken);

    List<Member> findByFamilyFamilyId(Long familyId);

    List<Member> findByFamilyFamilyIdOrderByMemberIdAsc(Long familyId);
    @Query("SELECT COUNT(m) FROM Member m WHERE m.family.familyId = :familyId")
    int countByFamilyId(@Param("familyId") Long familyId); // 멤버의 같은 패밀리 아이디 개수로 확인

}
