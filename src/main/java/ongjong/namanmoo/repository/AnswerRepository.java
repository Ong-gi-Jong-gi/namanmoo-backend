package ongjong.namanmoo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.answer.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnswerRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(final Answer answer) {
        em.persist(answer);
    }

    public Answer findById(final Long id) {
        return em.find(Answer.class, id);
    }

    public List<Answer> findAll() {
        return em.createQuery("from Answer", Answer.class).getResultList();
    }
}
