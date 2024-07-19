//package ongjong.namanmoo.service;
//
//import ongjong.namanmoo.domain.Family;
//import ongjong.namanmoo.domain.Member;
//import ongjong.namanmoo.dto.family.FamilyInviteResponse;
//import ongjong.namanmoo.dto.family.FamilyMemberDto;
//import ongjong.namanmoo.global.security.util.SecurityUtil;
//import ongjong.namanmoo.repository.FamilyRepository;
//import ongjong.namanmoo.repository.MemberRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//@Transactional
//@Rollback
//public class FamilyServiceTest {
//
//    @Mock
//    FamilyRepository familyRepository;
//
//    @Mock
//    MemberRepository memberRepository;
//
//    @InjectMocks
//    FamilyServiceImpl familyService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        setUpSecurityContext("testUser");
//    }
//
//    private void setUpSecurityContext(String loginId) {
//        UserDetails userDetails = mock(UserDetails.class);
//        when(userDetails.getUsername()).thenReturn(loginId);
//
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn(userDetails);
//
//        SecurityContext securityContext = mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    @Test
//    void testCreateFamily() {
//        // given
//        String loginId = "testUser";
//        Member familyOwner = new Member();
//        familyOwner.setLoginId(loginId);
//        familyOwner.setMemberId(1L);
//        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(familyOwner));
//        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        Family createdFamily = familyService.createFamily("Test Family", 4, "Owner");
//
//        // then
//        assertThat(createdFamily).isNotNull();
//        assertThat(createdFamily.getFamilyName()).isEqualTo("Test Family");
//        assertThat(createdFamily.getMaxFamilySize()).isEqualTo(4);
//    }
//
//    @Test
//    void testAddMemberToFamily() {
//        // given
//        String loginId = "testUser";
//        Member member = new Member();
//        member.setLoginId(loginId);
//        member.setMemberId(1L);
//        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(member));
//
//        Family family = new Family();
//        family.setFamilyId(1L);
//        family.setMaxFamilySize(4);
//        when(familyRepository.findById(anyLong())).thenReturn(Optional.of(family));
//        when(memberRepository.countByFamilyId(anyLong())).thenReturn(3);
//        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        familyService.addMemberToFamily(1L, "Member");
//
//        // then
//        assertThat(member.getFamily()).isEqualTo(family);
//    }
//
//    @Test
//    void testFindFamilyId() {
//        // given
//        String loginId = "testUser";
//        Member member = new Member();
//        member.setLoginId(loginId);
//        member.setMemberId(1L);
//        Family family = new Family();
//        family.setFamilyId(1L);
//        member.setFamily(family);
//        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(member));
//
//        // when
//        Long familyId = familyService.findFamilyId();
//
//        // then
//        assertThat(familyId).isEqualTo(family.getFamilyId());
//    }
//
//    @Test
//    void testGetFamilyMembersInfo() {
//        // given
//        String loginId = "testUser";
//        Member currentUser = new Member();
//        currentUser.setLoginId(loginId);
//        currentUser.setMemberId(1L);
//        Family family = new Family();
//        family.setFamilyId(1L);
//        currentUser.setFamily(family);
//        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(currentUser));
//
//        Member member1 = new Member();
//        member1.setLoginId("member1");
//        Member member2 = new Member();
//        member2.setLoginId("member2");
//        when(memberRepository.findByFamilyFamilyId(anyLong())).thenReturn(List.of(member1, member2));
//
//        // when
//        List<FamilyMemberDto> familyMembersInfo = familyService.getFamilyMembersInfo();
//
//        // then
//        assertThat(familyMembersInfo).hasSize(2);
//    }
//
//    @Test
//    void testGetFamilyInfoByInviteCode() {
//        // given
//        String inviteCode = "TESTCODE";
//        Family family = new Family();
//        family.setFamilyId(1L);
//        family.setFamilyName("Test Family");
//        family.setInviteCode(inviteCode);
//        when(familyRepository.findByInviteCode(anyString())).thenReturn(Optional.of(family));
//
//        Member member1 = new Member();
//        member1.setLoginId("member1");
//        Member member2 = new Member();
//        member2.setLoginId("member2");
//        when(memberRepository.findByFamilyFamilyId(anyLong())).thenReturn(List.of(member1, member2));
//
//        // when
//        FamilyInviteResponse response = familyService.getFamilyInfoByInviteCode(inviteCode);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getFamilyName()).isEqualTo("Test Family");
//        assertThat(response.getMembers()).hasSize(2);
//    }
//}
