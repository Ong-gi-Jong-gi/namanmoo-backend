package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
//    @Rollback(false)
    public void testmember() throws Exception{
        Member member = new Member();
        member.setName("member a");
        Member member1 = memberRepository.save(member);
        Long saveId = member1.getMemberId();
        Member findMember = memberRepository.findById(saveId).get();
        Assertions.assertThat(findMember.getMemberId()).isEqualTo(member.getMemberId());
        Assertions.assertThat(findMember.getName()).isEqualTo(member.getName());
        Assertions.assertThat(findMember).isEqualTo(member);

    }

  
}