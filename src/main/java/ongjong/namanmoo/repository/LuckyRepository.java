package ongjong.namanmoo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ongjong.namanmoo.domain.Lucky;
import java.util.List;

public interface LuckyRepository extends JpaRepository<Lucky,Long> {
    List<Lucky> findByFamilyFamilyId(Long familyId);
}
