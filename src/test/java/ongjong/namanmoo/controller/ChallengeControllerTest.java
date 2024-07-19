package ongjong.namanmoo.controller;

import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.domain.challenge.ChallengeType;
import ongjong.namanmoo.dto.challenge.CurrentChallengeDto;
import ongjong.namanmoo.dto.challenge.SaveChallengeRequest;
import ongjong.namanmoo.dto.lucky.CurrentLuckyDto;
import ongjong.namanmoo.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChallengeController.class)
public class ChallengeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChallengeService challengeService;
    @MockBean
    private MemberService memberService;
    @MockBean
    private LuckyService luckyService;
    @MockBean
    private AnswerService answerService;
    @MockBean
    private FamilyService familyService;
    @MockBean
    private AwsS3Service awsS3Service;
    @MockBean
    private SharedFileService sharedFileService;
    @MockBean
    private OpenAIClientService openAIClientService;
    @MockBean
    private FFmpegService ffmpegService;

    @Test
    @DisplayName("챌린지 시작")
    public void testSaveChallenge() throws Exception {
        SaveChallengeRequest request = new SaveChallengeRequest();
        request.setChallengeDate(1713888000000L);

        when(familyService.findFamilyId()).thenReturn(1L);
        when(luckyService.createLucky(anyLong(), anyLong())).thenReturn(true);
        when(answerService.createAnswer(anyLong(), anyLong())).thenReturn(true);

        // 보안설정 우회 , 테스트에서 인증된 사용자로 요청을 보냄
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new User("testuser", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges")
                        .with(csrf())  // Include CSRF token
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"challengeDate\":1713888000000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("Challenge created successfully"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("현재 진행 중인 챌린지 시작 날짜 반환")
    public void testGetChallengeStartDate() throws Exception {
        Long familyId = 1L;
        String challengeStartDate = "2024.07.19";  // 타임스탬프 값
        Long challengeStartDateTimestamp = DateUtil.getInstance().stringToTimestamp(challengeStartDate,DateUtil.FORMAT_4);

        Lucky lucky = new Lucky();
        lucky.setChallengeStartDate(challengeStartDate);  // Setter 사용
        when(familyService.findFamilyId()).thenReturn(familyId);
        when(luckyService.findCurrentLucky(familyId)).thenReturn(lucky);

        // Set up a mock SecurityContext
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new User("testuser", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges/startDate")
                        .with(csrf())  // Include CSRF token
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.challengeStartDate").value(challengeStartDateTimestamp));
    }

    @Test
    @DisplayName("오늘의 챌린지 조회 - 성공")
    public void testGetChallenge_Success() throws Exception {
        Long challengeDate = 1713888000000L;  // 예시 타임스탬프 (2024.07.19의 타임스탬프)
        Member mockMember = new Member();
        Challenge mockChallenge = new Challenge();
        mockChallenge.setChallengeId(1L);
        mockChallenge.setChallengeTitle("가족에게 전화하시오.");
        mockChallenge.setChallengeType(ChallengeType.NORMAL);

        CurrentChallengeDto.ChallengeInfo challengeInfo = new CurrentChallengeDto.ChallengeInfo(mockChallenge, 1, "2024.07.19");
        CurrentChallengeDto mockChallengeDto = new CurrentChallengeDto(false, challengeInfo);

        when(memberService.findMemberByLoginId()).thenReturn(mockMember);
        when(challengeService.findChallengesByMemberId(challengeDate, mockMember)).thenReturn(mockChallengeDto);

        // Set up a mock SecurityContext
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new User("testuser", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges/today")
                        .param("challengeDate", String.valueOf(challengeDate))
                        .with(csrf())  // Include CSRF token
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("Challenge found successfully"))
                .andExpect(jsonPath("$.data.isDone").value(false))
                .andExpect(jsonPath("$.data.challengeInfo.challengeId").value("1"))
                .andExpect(jsonPath("$.data.challengeInfo.challengeNumber").value("1"))
                .andExpect(jsonPath("$.data.challengeInfo.challengeTitle").value("가족에게 전화하시오."))
                .andExpect(jsonPath("$.data.challengeInfo.challengeType").value("NORMAL"))
                .andExpect(jsonPath("$.data.challengeInfo.challengeDate").value("2024.07.19"));
    }

    @Test
    @DisplayName("오늘의 챌린지 조회 - 챌린지 날짜가 13자리 숫자가 아닌 경우")
    public void testGetChallenge_InvalidDateLength() throws Exception {
        Long invalidChallengeDate = 123L; // 13자리 숫자가 아닌 경우

        // Set up a mock SecurityContext
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new User("testuser", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges/today")
                        .param("challengeDate", String.valueOf(invalidChallengeDate))
                        .with(csrf())  // Include CSRF token
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message").value("Challenge date must be a 13-number"));
    }

    @Test
    @DisplayName("오늘의 챌린지 조회 - 챌린지를 찾을 수 없는 경우")
    public void testGetChallenge_NotFound() throws Exception {
        Long challengeDate = 1713888000000L; // 예시 타임스탬프
        Member mockMember = new Member();
        CurrentChallengeDto mockChallengeDto = null; // 챌린지 없음

        when(memberService.findMemberByLoginId()).thenReturn(mockMember);
        when(challengeService.findChallengesByMemberId(challengeDate, mockMember)).thenReturn(mockChallengeDto);

        // Set up a mock SecurityContext
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new User("testuser", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges/today")
                        .param("challengeDate", String.valueOf(challengeDate))
                        .with(csrf())  // Include CSRF token
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message").value("Challenge not found"));
    }


}
