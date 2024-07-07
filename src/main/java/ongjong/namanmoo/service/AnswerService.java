package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.*;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final LuckyRepository luckyRepository;

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
                if (challenge.getChallengeType() == ChallengeType.GROUP_PARENT){            // 만약 member가 아빠 일경우, challenge가 CGROUP이면 ANSWER 생성 X
                    if (member.getRole().equals("아들") || member.getRole().equals("딸")){
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.GROUP_CHILD){
                    if (member.getRole().equals("아빠") || member.getRole().equals("엄마")){
                        continue;
                    }
                }

                Answer answer = new Answer(); // Answer 객체 생성
                answer.setMember(member);
//                String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
//                answer.setCreateDate(currentDateStr);
                answer.setCheckChallenge(false);
                answer.setChallenge(challenge);

                if (challenge.getChallengeType()== ChallengeType.NORMAL) {
                    answer.setAnswerType(AnswerType.NORMAL);
                } else if (challenge.getChallengeType()== ChallengeType.GROUP_CHILD) {
                    answer.setAnswerType(AnswerType.GROUP);
                } else if (challenge.getChallengeType()== ChallengeType.GROUP_PARENT) {
                    answer.setAnswerType(AnswerType.GROUP);
                } else if (challenge.getChallengeType()== ChallengeType.FACETIME) {
                    answer.setAnswerType(AnswerType.FACETIME);

                    // FaceTimeC일 경우 FaceTimeAnswer 생성 및 저장
                    FaceTimeAnswer faceTimeAnswer = new FaceTimeAnswer();
                    faceTimeAnswer.setLucky(findCurrentLucky(member.getFamily().getId()));
                    faceTimeAnswerRepository.save(faceTimeAnswer);

                    // FaceTimeAnswer의 ID를 Answer의 answerContent에 저장
                    answer.setAnswerContent(String.valueOf(faceTimeAnswer.getFaceTimeAnswerId()));
                } else if (challenge.getChallengeType()== ChallengeType.PHOTO) {
                    answer.setAnswerType(AnswerType.PHOTO);
                } else if (challenge.getChallengeType()== ChallengeType.VOICE) {
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

    @Transactional(readOnly = true)
    public Long findAnswerByChallengeMember(Challenge challenge, Member member) throws Exception{
        Answer answer = answerRepository.findByChallengeAndMember(challenge, member).orElse(null);
        return getTimeStamp(answer.getCreateDate(),"yyyy.MM.dd");
    }

    @Transactional(readOnly = true)
    public Long getTimeStamp(String answerDate, String format) throws Exception{       //  "yyyy.MM.dd"형식의 문자열을 timeStamp로 바꾸기
        if ((answerDate == null || answerDate.equals("")) || (format == null || format.equals(""))) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDate date = LocalDate.parse(answerDate, formatter);
        LocalDateTime dateTime = date.atStartOfDay(); // LocalDate를 LocalDateTime으로 변환 (00:00:00)
        return Timestamp.valueOf(dateTime).getTime();
    }


    @Transactional(readOnly = true)
    public List<Answer> findAnswerByChallenge(Challenge challenge){
        return answerRepository.findByChallenge(challenge);
    }

//    public Answer saveAnswer(Long challengeId, String answer){
//        Challenge challenge =  challengeService.findChallengeById(challengeId);
//        Optional<Member> member = memberService.findLoginMember();
//    }

    @Transactional(readOnly = true)
    public Lucky findCurrentLucky(Long familyId) {       // 현재 진행중인 lucky id 조회
        List<Lucky> luckies = luckyRepository.findByFamilyId(familyId);
        for (Lucky lucky : luckies) {
            if (lucky.isRunning()) {
                return lucky; // 현재 진행되고있는 luckyid 반환
            }
        }
        return null;
    }

    public void saveCreateDate(Challenge challenge){                // 오늘의 챌리지를 조회할때 해당 challenge에 해당하는 answer의 createDate에 오늘의 날짜를 저장한다.
        List <Answer> answerList = answerRepository.findByChallenge(challenge);
        for(Answer answer : answerList){
            if (answer.getCreateDate().isEmpty()){      // answer의 createDate가 비어있을 경우에만 오늘의 날짜 set
                String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                answer.setCreateDate(currentDateStr);
            }
        }
    }

}
