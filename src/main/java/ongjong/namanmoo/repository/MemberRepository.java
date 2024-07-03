package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}