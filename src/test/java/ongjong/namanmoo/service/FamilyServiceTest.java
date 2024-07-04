package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Rollback
public class FamilyServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FamilyService familyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private void mockSecurityContext(String loginId) {
        UserDetails user = User.withUsername(loginId).password("password").roles("USER").build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities()));
    }

    @Test
    void testCreateFamily() {
        // given
        String loginId = "testUser";
        mockSecurityContext(loginId);

        Member member = new Member();
        member.setLoginId(loginId);
        member.setMemberId(1L);
        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(member));

        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Family createdFamily = familyService.createFamily("Test Family", 4, "Owner");

        // then
        assertThat(createdFamily).isNotNull();
        assertThat(createdFamily.getFamilyName()).isEqualTo("Test Family");
        assertThat(createdFamily.getMaxFamilySize()).isEqualTo(4);
        assertThat(createdFamily.getFamilyOwnerId()).isEqualTo(member.getMemberId());
        assertThat(createdFamily.getCurrentFamilySize()).isEqualTo(1);
    }

    @Test
    void testAddMemberToFamily() {
        // given
        String loginId = "testUser";
        mockSecurityContext(loginId);

        Member member = new Member();
        member.setLoginId(loginId);
        member.setMemberId(1L);
        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(member));

        Family family = new Family();
        family.setId(1L);
        family.setMaxFamilySize(4);
        family.setCurrentFamilySize(1);
        when(familyRepository.findById(any(Long.class))).thenReturn(Optional.of(family));

        // when
        familyService.addMemberToFamily(1L, "Member");

        // then
        assertThat(member.getFamily()).isEqualTo(family);
        assertThat(family.getMembers()).contains(member);
        assertThat(family.getCurrentFamilySize()).isEqualTo(2);
    }

    @Test
    void testAddMemberToFullFamily() {
        // given
        String loginId = "testUser";
        mockSecurityContext(loginId);

        Member member = new Member();
        member.setLoginId(loginId);
        member.setMemberId(1L);
        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(member));

        Family family = new Family();
        family.setId(1L);
        family.setMaxFamilySize(1);
        family.setCurrentFamilySize(1);
        when(familyRepository.findById(any(Long.class))).thenReturn(Optional.of(family));

        // when/then
        assertThrows(IllegalStateException.class, () -> familyService.addMemberToFamily(1L, "Member"));
    }
}