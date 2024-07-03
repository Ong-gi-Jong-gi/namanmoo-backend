package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByFamilyId(Long familyId);
}
