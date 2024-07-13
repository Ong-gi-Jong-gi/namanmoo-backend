package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.challenge.ChallengeDetailsDto;
import ongjong.namanmoo.dto.challenge.GroupChallengeDto;
import ongjong.namanmoo.dto.recapMember.MemberAndCountDto;
import ongjong.namanmoo.dto.recapMember.MemberPhotosAnswerDto;
import ongjong.namanmoo.dto.recapMember.MemberYouthAnswerDto;
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

    // 각 member의 memberimg와 특정 번호의 챌린지 답변을 묶어 반환
    List<MemberYouthAnswerDto> getAnswerByMember(List<Member> members) throws Exception;

    MemberPhotosAnswerDto getPhotoByMember(List<Member> members) throws Exception;

    // 챌린지 상세조회 중복요소 매핑
    ChallengeDetailsDto getChallengeDetails(Challenge challenge, Member member) throws Exception;
}
