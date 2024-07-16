package ongjong.namanmoo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import ongjong.namanmoo.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByInviteCode(String inviteCode);
//    Optional<Family> findByLuckyId(Long luckyId); // TODO

    boolean existsByInviteCode(String inviteCode);
}

