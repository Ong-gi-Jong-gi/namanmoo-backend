package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.challenge.ChallengeDetailsDto;
import ongjong.namanmoo.dto.recap.AppreciationDto;
import ongjong.namanmoo.dto.recap.MemberAndCountDto;

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

    // member 정보와 각 member에 대한 답변 입력 횟수 반환
    List<MemberAndCountDto> getMemberAndCount(Lucky lucky);

    // 챌린지 상세조회 중복요소 매핑
    ChallengeDetailsDto getChallengeDetails(Challenge challenge, Member member) throws Exception;

//    List<AppreciationDto> getAppreciations(Lucky lucky) throws Exception;

}
