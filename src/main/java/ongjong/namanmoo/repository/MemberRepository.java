package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId (String loginId);

    boolean existsByLoginId (String loginId);

    Optional<Member> findByRefreshToken(String refreshToken);

    List<Member> findByFamilyId(Long familyId);
}
