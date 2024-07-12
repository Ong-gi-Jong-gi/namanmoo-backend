package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.challenge.GroupChallengeDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChallengeService {

    // 현재 진행하고 있는 행운이의 챌린지 리스트 가져오기
    List<Challenge> findChallenges(Long challengeDate) throws Exception;

    // challenge id로 challenge 찾기
    Challenge findChallengeById(Long id);

    // 회원 아이디로 오늘의 챌린지 조회
    List<Challenge> findChallengesByMemberId(Long challengeDate, Member member) throws Exception;

    // 오늘의 챌린지 조회
    Challenge findOneInCurrentChallenges(List<Challenge> challenges) throws Exception;

    // 현재 날짜와 챌린지 시작 날짜를 비교하여 몇번째 챌린지를 진행중인지 반환
    Integer findCurrentNum(Long challengeDate) throws Exception;

    // 현재 진행하고 있는 챌린지를 행운이의 챌린지 길이만큼 가져오기
    List<Challenge> findRunningChallenges() throws Exception;


    // groupChallenge 조회를 위한 dto  (부모와 자식의 challenge 질문 구분하기)
    @Transactional(readOnly = true)
    GroupChallengeDto createGroupChallenge(Challenge challenge, Long timeStamp, boolean isComplete, List<Answer> answers);
}
