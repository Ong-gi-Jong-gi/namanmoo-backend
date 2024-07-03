package ongjong.namanmoo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ongjong.namanmoo.domain.Member;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public Long save(Member member){
        em.persist(member);
        return member.getMemberId();
    }
    public Member find(Long memberId){
        return em.find(Member.class, memberId);
    }
}
