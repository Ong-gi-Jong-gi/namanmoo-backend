package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyRepository extends JpaRepository<Family,Long> {

}
