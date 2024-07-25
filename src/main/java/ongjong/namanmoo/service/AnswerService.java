package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.challenge.ChallengeDetailsDto;
import ongjong.namanmoo.dto.recap.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AnswerService {

    // 답변 생성칸 만들기
    boolean createAnswer(Long familyId, Long challengeDate) throws Exception;

    // challenge와 member로 answer찾기
    boolean findIsCompleteAnswer(Challenge challenge, Member member);

    // 챌린지와 멤버로 챌린지 답변 생성날짜 찾기
    Long findDateByChallengeMember(Challenge challenge, Member member) throws Exception;

    // 가족 구성원들의 답변 유무 검사
    boolean isAnyAnswerComplete(Challenge challenge, Family family);

    // 가족 구성원들의 답변 수정 유무 검사 (FaceChallenge 조회에서 사용)
    boolean isAnyAnswerModified(Challenge challenge, Family family);

    // 답변 수정
    Answer modifyAnswer(Long challengeId, String answerContent) throws Exception;

    // 챌린지와 멤버로 답변 찾기
    Optional<Answer> findAnswerByChallengeAndMember(Challenge challenge, Member member);

    // 답변 저장
    void saveAnswer(Answer answer) throws Exception;

    // 말풍선 클릭하면 말풍선 확인 변수 끄기
    void offBalloon(Long challengeDate) throws Exception;

    // 챌린지와 멤버를 받아서 답변 리스트 반환
    List<Answer> findAnswersByChallenges(Challenge challenge, Member member);

    List<MemberDto> getAnswersByMember(Long luckyId, int challengeNum1, int challengeNum2, Class<? extends MemberDto> dtoClass) throws Exception;

    Challenge findFastestAnsweredChallenge(Lucky lucky) throws Exception;

    long calculateLatestResponseTime(List<Answer> answers);

    List<MemberYouthAnswerDto> getYouthByMember(Long luckyId, int challengeNum1, int challengeNum2) throws Exception;

    List<MemberAppreciationDto> getAppreciationByMember(Long luckyId, int challengeNum1, int challengeNum2) throws Exception;

    MemberDto createDto(Class<? extends MemberDto> dtoClass, Member member, String answer1, String answer2);

    MemberPhotosAnswerDto getPhotos(Long luckyId) throws Exception;

    // facetime에 대한 answerList를 반환
    MemberFacetimeDto getFacetimeAnswerList(Long luckyId) throws Exception ;

    // 챌린지 상세조회 중복요소 매핑
    ChallengeDetailsDto getChallengeDetails(Challenge challenge, Member member) throws Exception;

}
