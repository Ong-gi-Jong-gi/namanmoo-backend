package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.DateUtil;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.*;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final FaceTimeAnswerRepository faceTimeAnswerRepository;
    private final LuckyRepository luckyRepository;
    private final ChallengeService challengeService;
    private final FamilyRepository familyRepository;

    public boolean createAnswer(Long familyId, Long challengeDate) throws Exception {

        // 가족 ID로 해당 가족의 회원들을 조회
        List<Member> members = memberRepository.findByFamilyFamilyId(familyId);
        Optional<Family> family  = familyRepository.findById(familyId);
        // 모든 챌린지를 조회
        List<Challenge> challenges = challengeService.findRunningChallenges(challengeDate); // todo 챌린지가 30개가 넘어갈때를 고려해야함  -> 답변 테이블이 challenge가 시작 될때마다 30개씩 들어가야함

        // 가족에 해당하는 회원이 없으면 false를 반환
        if (members.size() != family.get().getMaxFamilySize()) {
            return false;
        }
        DateUtil dateUtil = DateUtil.getInstance();
        // 각 회원마다 모든 챌린지에 대해 답변 생성
        for (Member member : members) {
            String strChallengeDate = dateUtil.getDateStirng(challengeDate);        // timestamp인 challengedate를 string "yyyy.MM.dd" 으로 변환
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
                answer.setBubbleVisible(false);
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
    public boolean findIsCompleteAnswer(Challenge challenge,Member member){
        return answerRepository.findByChallengeAndMember(challenge, member)
                .map(Answer::isBubbleVisible)      // answer가 존재할 경우 ischeckChallenge 값을 반환
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Long findAnswerByChallengeMember(Challenge challenge, Member member) throws Exception{
        Answer answer = answerRepository.findByChallengeAndMember(challenge, member).orElse(null);
        assert answer != null;
        return getTimeStamp(answer.getCreateDate(),"yyyy.MM.dd");
    }

    @Transactional(readOnly = true)
    public Long getTimeStamp(String answerDate, String format) throws Exception{       //  "yyyy.MM.dd"형식의 문자열을 timeStamp로 바꾸기
        if ((answerDate == null || answerDate.isEmpty()) || (format == null || format.isEmpty())) {
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
        List<Lucky> luckies = luckyRepository.findByFamilyFamilyId(familyId);
        for (Lucky lucky : luckies) {
            if (lucky.isRunning()) {
                return lucky; // 현재 진행되고있는 luckyid 반환
            }
        }
        return null;
    }

//    public void saveCreateDate(Challenge challenge){                // 오늘의 챌리지를 조회할때 해당 challenge에 해당하는 answer의 createDate에 오늘의 날짜를 저장한다.
//        List <Answer> answerList = answerRepository.findByChallenge(challenge);
//        for(Answer answer : answerList){
//            if (answer.getCreateDate().isEmpty()){      // answer의 createDate가 비어있을 경우에만 오늘의 날짜 set
//                String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
//                answer.setCreateDate(currentDateStr);
//            }
//        }
//    }
    @Transactional(readOnly = true)
    public boolean checkUserResponse(Member member, String createDate) {
        return answerRepository.existsByMemberAndCreateDateAndAnswerContentIsNotNull(member, createDate);
    }


    @Transactional
    public void offBalloon(Long challengeDate) throws Exception {
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId())
                .orElseThrow(() -> new Exception("회원이 존재하지 않습니다"));

        // 타임스탬프를 yyyy.MM.dd 형식의 문자열로 변환
        Instant instant = Instant.ofEpochMilli(challengeDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneId.systemDefault());
        String createDate = formatter.format(instant);

        Optional<Answer> answer = answerRepository.findByMemberAndCreateDate(member, createDate);
        if (answer.isPresent()) {
            answer.get().setBubbleVisible(false);
        } else {
            throw new IllegalArgumentException("No answer found for the given loginId and createDate");
        }
    }
}
