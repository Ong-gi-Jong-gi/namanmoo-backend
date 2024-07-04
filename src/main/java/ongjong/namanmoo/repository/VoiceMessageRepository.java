package ongjong.namanmoo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.VoiceMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface VoiceMessageRepository extends JpaRepository<VoiceMessage, Long> {}

//@Repository
//@RequiredArgsConstructor
//public class VoiceMessageRepository {
//
//    @PersistenceContext
//    private EntityManager em;
//
//    public void save(final VoiceMessage voiceMessage) {
//        em.persist(voiceMessage);
//    }
//
//    public VoiceMessage findById(final Long id) {
//        return em.find(VoiceMessage.class, id);
//    }
//
//    public List<VoiceMessage> findAll() {
//        return em.createQuery("select m from VoiceMessage m", VoiceMessage.class).getResultList();
//    }
//}
