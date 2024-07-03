package ongjong.namanmoo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ObjectRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(Object o) {
        em.persist(o);
    }

    public void update(Object o) {
        em.merge(o);
    }

    public void delete(Object o) {
        em.remove(o);
    }

//    public <T> T find(Class<T> clazz, Object id) {
//        return em.find(clazz, id);
//    }
}
