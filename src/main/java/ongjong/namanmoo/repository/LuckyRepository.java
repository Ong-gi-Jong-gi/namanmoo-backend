package ongjong.namanmoo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Lucky;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface LuckyRepository extends JpaRepository<Lucky, Integer> {}

//@Repository
//@RequiredArgsConstructor
//public class LuckyRepository {
//
//    @PersistenceContext
//    private EntityManager em;
//
//    public void save(Lucky o) {
//        em.persist(o);
//    }
//
//    public void update(Lucky o) {
//        em.merge(o);
//    }
//
//    public void delete(Lucky o) {
//        em.remove(o);
//    }
//
////    public <T> T find(Class<T> clazz, Lucky id) {
////        return em.find(clazz, id);
////    }
//}
