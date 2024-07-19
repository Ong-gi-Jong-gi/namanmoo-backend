package ongjong.namanmoo.repository;

import jakarta.persistence.EntityManager;
import ongjong.namanmoo.domain.LogInRole;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.MemberRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("로그인 테스트")
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    @AfterEach
    public void after() {
        em.clear();
    }

    @Test
    public void 회원저장_성공() throws Exception {
        //given
        Member member = Member.builder()
                .loginId("loginId")
                .password("1234567890")
                .name("Member1")
                .nickname("Nickname1")
                .role(String.valueOf(MemberRole.SON))
//                .challengeMemberCount(1L)
//                .checkChallenge(false)
                .logInRole(LogInRole.USER)
                .build();

        //when
        Member saveMember = memberRepository.save(member);

        Optional<Member> optionalMember = memberRepository.findByLoginId("loginId");

        //then
        Member findMember = memberRepository
                .findById(saveMember.getMemberId())
                .orElseThrow(() -> new RuntimeException("저장된 회원이 없습니다"));


        assertThat(findMember).isSameAs(saveMember);
        assertThat(findMember).isSameAs(member);
        assertThat(memberRepository.existsByLoginId(member.getLoginId())).isTrue();
        assertTrue(optionalMember.isPresent(), "Member should be present");
        assertEquals(optionalMember.get(), member, "로그인아이디가 같다"); // 멤버 객체를 추출하여 비교
    }

//    @Test
//    @Transactional
////    @Rollback(false)
//    public void testmember() throws Exception {
//        Member member = new Member();
//        member.setName("member a");
//        Member member1 = memberRepository.save(member);
//        Long saveId = member1.getMemberId();
//        Member findMember = memberRepository.findById(saveId).get();
//        Assertions.assertThat(findMember.getMemberId()).isEqualTo(member.getMemberId());
//        Assertions.assertThat(findMember.getName()).isEqualTo(member.getName());
//        Assertions.assertThat(findMember).isEqualTo(member);
//    }

    public void 오류_회원가입시_아이디가_없음() throws Exception {
        //given
        Member member = Member.builder()
                .password("1234567890")
                .name("Member1")
                .nickname("Nickname1")
                .role(String.valueOf(MemberRole.SON))
//                .challengeMemberCount(1L)
//                .checkChallenge(false)
                .logInRole(LogInRole.USER)
                .build();
        //when

        //then
        assertThrows(Exception.class, () -> memberRepository.save(member));
    }

    @Test
    public void 오류_회원가입시_중복된_아이디_존재() throws Exception {
        //given
        Member member1 = Member.builder()
                .loginId("loginId")
                .password("1234567890")
                .name("Member1")
                .nickname("Nickname1")
                .role(String.valueOf(MemberRole.SON))
//                .challengeMemberCount(1L)
//                .checkChallenge(false)
                .logInRole(LogInRole.USER)
                .build();

        Member member2 = Member.builder()
                .loginId("loginId")
                .password("0987654321")
                .name("Member2")
                .nickname("Nickname2")
                .role(String.valueOf(MemberRole.FATHER))
//                .challengeMemberCount(1L)
//                .checkChallenge(false)
                .logInRole(LogInRole.USER)
                .build();
        memberRepository.save(member1);

        //when

        //then
        assertThrows(Exception.class, () -> memberRepository.save(member2));
    }

//    @Test
//    public void 성공_회원수정() throws Exception {
//        //given
//        Member member1 = Member.builder()
//                .loginId("loginId")
//                .password("1234567890")
//                .name("Member1")
//                .nickname("NickName1")
//                .role(MemberRole.SON)
////                .challengeMemberCount(1L)
////                .checkChallenge(false)
//                .logInRole(LogInRole.USER)
//                .build();
//        memberRepository.save(member1);
//
//
//        String updatePassword = "updatePassword";
//        String updateName = "updateName";
//        String updateNickname = "updateNickname";
//
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//        //when
//        Member findMember = memberRepository.findById(member1.getMemberId()).orElseThrow(() -> new RuntimeException("저장된 회원이 없습니다"));
//        findMember.updateName(updateName);
//        findMember.updateNickname(updateNickname);
//        findMember.updatePassword(passwordEncoder,updatePassword);
//        em.flush();
//
//        //then
//
//        Member findUpdateMember = memberRepository.findById(findMember.getMemberId()).orElseThrow(() -> new RuntimeException("수정오류"));
//        assertThat(findUpdateMember).isEqualTo(dfindMember);
//        assertThat(passwordEncoder.matches(updatePassword, findUpdateMember.getPassword())).isTrue();
//        assertThat(findUpdateMember.getName()).isEqualTo(updateName);
//        assertThat(findUpdateMember.getName()).isNotEqualTo("Member1");
//    }
//
}