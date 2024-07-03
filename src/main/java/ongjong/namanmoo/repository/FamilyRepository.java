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
    static Optional<Family> findByInviteCode(String inviteCode) {
        return Optional.empty();
    }

    Optional<Family> findByFamilyId(Long familyId);
}

//@Repository
//public class FamilyRepository {
//
//    @PersistenceContext
//    private static EntityManager em;
//
//    // 가족 저장
//    public Family save(Family family) {
//        if (family.getFamilyId() == null) {
//            em.persist(family);
//        } else {
//            em.merge(family);
//        }
//        return family;
//    }
//
//    // 모든 가족 찾기
//    public List<Family> findAll() {
//        TypedQuery<Family> query = em.createQuery("SELECT f FROM Family f", Family.class);
//        return query.getResultList();
//    }
//
//    // ID로 가족 찾기
//    public Optional<Family> findById(Long id) {
//        Family family = em.find(Family.class, id);
//        return Optional.ofNullable(family);
//    }
//
//    // 초대 코드로 가족 찾기
//    public static Optional<Family> findByInviteCode(String inviteCode) {
//        TypedQuery<Family> query = em.createQuery("SELECT f FROM Family f WHERE f.inviteCode = :inviteCode", Family.class);
//        query.setParameter("inviteCode", inviteCode);
//        List<Family> result = query.getResultList();
//        if (result.isEmpty()) {
//            return Optional.empty();
//        } else {
//            return Optional.of(result.get(0));
//        }
//    }
//
//    // 가족 삭제
//    public void delete(Family family) {
//        if (em.contains(family)) {
//            em.remove(family);
//        } else {
//            em.remove(em.merge(family));
//        }
//    }
//}