package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.global.security.util.DateUtil;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.*;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final MemberServiceImpl memberServiceImpl;
    private final MemberService memberService;
    private final FamilyRepository familyRepository;
    private final ChallengeService challengeService;

    public boolean createAnswer(Long familyId, Long challengeDate) throws Exception {

        // 가족 ID로 해당 가족의 회원들을 조회
        List<Member> members = memberRepository.findByFamilyFamilyId(familyId);
        Optional<Family> family  = familyRepository.findById(familyId);
        // 모든 챌린지를 조회
        List<Challenge> challenges = challengeService.findRunningChallenges(challengeDate);

        // 가족이 다 방에 들어오지 않았을 경우 null 반환
        if (members.size() != family.get().getMaxFamilySize()) {
            return false;
        }
        DateUtil dateUtil = DateUtil.getInstance();
        // 각 회원마다 모든 챌린지에 대해 답변 생성
        for (Member member : members) {
            String strChallengeDate = dateUtil.timestampToString(challengeDate);        // timestamp인 challengedate를 string "yyyy.MM.dd" 으로 변환
            for (Challenge challenge : challenges) {                            // 현재 챌린지의 개수 만큼 answer 생성 -> 챌린지의 개수가 30개가 넘었을 경우 stop
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
                    answer.setCreateDate(strChallengeDate);
                } else if (challenge.getChallengeType()== ChallengeType.GROUP_CHILD) {
                    answer.setAnswerType(AnswerType.GROUP);
                    answer.setCreateDate(strChallengeDate);
                } else if (challenge.getChallengeType()== ChallengeType.GROUP_PARENT) {
                    answer.setAnswerType(AnswerType.GROUP);
                    answer.setCreateDate(strChallengeDate);
                } else if (challenge.getChallengeType()== ChallengeType.FACETIME) {
                    answer.setAnswerType(AnswerType.FACETIME);
                    answer.setCreateDate(strChallengeDate);

                    // FaceTimeC일 경우 FaceTimeAnswer 생성 및 저장
                    FaceTimeAnswer faceTimeAnswer = new FaceTimeAnswer();
                    faceTimeAnswer.setLucky(findCurrentLucky(member.getFamily().getFamilyId()));
                    faceTimeAnswerRepository.save(faceTimeAnswer);

                    // FaceTimeAnswer의 ID를 Answer의 answerContent에 저장
                    answer.setAnswerContent(String.valueOf(faceTimeAnswer.getFaceTimeAnswerId()));
                } else if (challenge.getChallengeType()== ChallengeType.PHOTO) {
                    answer.setAnswerType(AnswerType.PHOTO);
                    answer.setCreateDate(strChallengeDate);
                } else if (challenge.getChallengeType()== ChallengeType.VOICE) {
                    answer.setAnswerType(AnswerType.VOICE);
                    answer.setCreateDate(strChallengeDate);
                }
                // challengeDate를 1일 증가
                strChallengeDate = dateUtil.addDaysToStringDate(strChallengeDate,1);
                // 생성된 Answer 저장
                answerRepository.save(answer);
            }
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean findIsCompleteAnswer(Challenge challenge,Member member){         // challenge와 member로 answer찾기
        Optional<Answer> answer = answerRepository.findByChallengeAndMember(challenge, member);
        return answer.map(a -> a.getAnswerContent() != null).orElse(false); // answercontent 가 존재하지 않을 경우 false, 존재할 경우 true 반환
    }

    @Transactional(readOnly = true)
    public Long findDateByChallengeMember(Challenge challenge) throws Exception{       // answer의 createDate를 timeStamp로 바꾸기
        Member member = memberService.findMemberByLoginId();
        Optional<Answer> answer = answerRepository.findByChallengeAndMember(challenge, member);
        DateUtil dateUtil = DateUtil.getInstance();
        return dateUtil.stringToTimestamp(answer.get().getCreateDate(),"yyyy.MM.dd");
    }


    @Transactional(readOnly = true)
    public List<Answer> findAnswerByChallenge(Challenge challenge){     // challenge로 answer 리스트 찾기
        return answerRepository.findByChallenge(challenge);
    }

//    public Answer saveAnswer(Long challengeId, String answer){
//        Challenge challenge =  challengeService.findChallengeById(challengeId);
//        Optional<Member> member = memberService.findLoginMember();
//    }

    @Transactional(readOnly = true)
    public Lucky findCurrentLucky(Long familyId) {       // 현재 진행중인 lucky id 조회
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(familyId);
        for (Lucky lucky : luckies) {
            if (lucky.isRunning()) {
                return lucky; // 현재 진행되고있는 luckyid 반환
            }
        }
        return null;
    }

//    // 로그인한 멤버를 찾고 해당 멤버가 작성한 answer중에 request로 받은 challengeId로 answer를 찾는다. 그리고 request로 받은 answer를 answer_content에 넣는다.
//    public boolean modifyAnswer(Long challengeId, String newAnswer) throws Exception{
//        Optional<Member> member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId());
//        Optional<Challenge> challenge = challengeRepository.findById(challengeId);
//        Optional<Answer> answer = answerRepository.findByChallengeAndMember(challenge.get(), member.get()) ;
//        answer.get().setAnswerContent(newAnswer);
//        answerRepository.save(answer.get());
//    }

    public void saveCreateDate(Challenge challenge){                // 오늘의 챌리지를 조회할때 해당 challenge에 해당하는 answer의 createDate에 오늘의 날짜를 저장한다.
        List <Answer> answerList = answerRepository.findByChallenge(challenge);
        for(Answer answer : answerList){
            if (answer.getCreateDate().isEmpty()){      // answer의 createDate가 비어있을 경우에만 오늘의 날짜 set
                String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                answer.setCreateDate(currentDateStr);
            }
        }
    }

    public Optional<Answer> findAnswerByChallengeAndMember(Challenge challenge, Member member) {
        return answerRepository.findByChallengeAndMember(challenge, member);
    }

    public void saveAnswer(Answer answer) {
        answerRepository.save(answer);
    }

}
