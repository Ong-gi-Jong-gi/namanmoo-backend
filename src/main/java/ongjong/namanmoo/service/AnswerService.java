package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.*;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.repository.AnswerRepository;
import ongjong.namanmoo.repository.ChallengeRepository;
import ongjong.namanmoo.repository.FaceTimeAnswerRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final ChallengeRepository challengeRepository;
    private final MemberRepository memberRepository;
    private final FaceTimeAnswerRepository faceTimeAnswerRepository;
    private final LuckyService luckyService;

    public boolean createAnswer(Long familyId) {

        // 가족 ID로 해당 가족의 회원들을 조회
        List<Member> members = memberRepository.findByFamilyId(familyId);

        // 모든 챌린지를 조회
        List<Challenge> challenges = challengeRepository.findAll();

        // 가족에 해당하는 회원이 없으면 false를 반환
        if (members.isEmpty()) {
            return false;
        }

        // 각 회원마다 모든 챌린지에 대해 답변 생성
        for (Member member : members) {
            for (Challenge challenge : challenges) {
                Answer answer = new Answer(); // Answer 객체 생성

                answer.setMember(member);
                answer.setCreateDate(new Timestamp(System.currentTimeMillis()));
                answer.setCheckChallenge(false);
                answer.setChallenge(challenge);

                if (challenge instanceof NormalC) {
                    answer.setAnswerType(AnswerType.NORMAL);
                } else if (challenge instanceof ChildGroup) {       // TODO: 만약 member가 아빠 일경우, challenge가 CGROUP이면 ANSWER 생성 X
                    answer.setAnswerType(AnswerType.GROUP);
                } else if (challenge instanceof FaceTimeC) {
                    answer.setAnswerType(AnswerType.FACETIME);

                    // FaceTimeC일 경우 FaceTimeAnswer 생성 및 저장
                    FaceTimeAnswer faceTimeAnswer = new FaceTimeAnswer();
                    faceTimeAnswer.setLucky(luckyService.findCurrentLucky(member.getFamily().getId()));
                    faceTimeAnswerRepository.save(faceTimeAnswer);

                    // FaceTimeAnswer의 ID를 Answer의 answerContent에 저장
                    answer.setAnswerContent(String.valueOf(faceTimeAnswer.getFaceTimeAnswerId()));
                } else if (challenge instanceof PhotoC) {
                    answer.setAnswerType(AnswerType.PHOTO);
                } else if (challenge instanceof VoiceC) {
                    answer.setAnswerType(AnswerType.VOICE);
                }

                // 생성된 Answer 저장
                answerRepository.save(answer);
            }
        }

        return true;
    }


    @Transactional(readOnly = true)
    public boolean findIsCompleteAnswer(Challenge challenge,Member member){
        return answerRepository.findByChallengeAndMember(challenge, member)
                .map(Answer::isCheckChallenge)      // answer가 존재할 경우 ischeckChallenge 값을 반환
                .orElse(false);
    }

    public List<Answer> findAnswerByChallenge(Challenge challenge){
        return answerRepository.findByChallenge(challenge);
    }

//    public Answer saveAnswer(Long challengeId, String answer){
//        Challenge challenge =  challengeService.findChallengeById(challengeId);
//        Optional<Member> member = memberService.findLoginMember();
//    }

}
