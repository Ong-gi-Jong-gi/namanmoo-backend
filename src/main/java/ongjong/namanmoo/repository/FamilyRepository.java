package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FamilyRepository extends JpaRepository<Family, Long> {
    static Optional<Family> findByInviteCode(String inviteCode) {
        return Optional.empty();
    }

    Optional<Family> findById(Long familyId);
}
