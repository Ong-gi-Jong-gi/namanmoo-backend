package ongjong.namanmoo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ongjong.namanmoo.domain.Lucky;
import java.util.List;
import java.util.Optional;

public interface LuckyRepository extends JpaRepository<Lucky,Long> {
    Lucky getLuckyByLuckyId(Long luckyId);
    List<Lucky> findByFamilyFamilyId(Long familyId);
    Optional<Lucky> findByFamilyFamilyIdAndRunningTrue(Long familyId);

}
