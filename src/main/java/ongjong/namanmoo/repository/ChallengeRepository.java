package ongjong.namanmoo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.challenge.Challenge;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChallengeRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(Challenge challenge) {
        em.persist(challenge);
    }

    public Challenge findById(String id) {
        return em.find(Challenge.class, id);
    }

    public List<Challenge> findAll() {
        return em.createQuery("from Challenge", Challenge.class).getResultList();
    }
}
