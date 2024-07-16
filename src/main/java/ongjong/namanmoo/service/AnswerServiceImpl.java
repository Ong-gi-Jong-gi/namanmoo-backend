package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.*;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.dto.challenge.ChallengeDetailsDto;
import ongjong.namanmoo.dto.recap.MemberAppreciationDto;
import ongjong.namanmoo.dto.recap.MemberDto;
import ongjong.namanmoo.dto.recap.MemberPhotosAnswerDto;
import ongjong.namanmoo.dto.recap.MemberYouthAnswerDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ongjong.namanmoo.domain.answer.AnswerType.PHOTO;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final LuckyRepository luckyRepository;
    private final FamilyRepository familyRepository;
    private final ChallengeRepository challengeRepository;

    private final LuckyService luckyService;
    private final ChallengeService challengeService;
    private final MemberService memberService;

    // 답변 생성칸 만들기
    @Override
    public boolean createAnswer(Long familyId, Long challengeDate) throws Exception {

        // 가족 ID로 해당 가족의 회원들을 조회
        List<Member> members = memberRepository.findByFamilyFamilyId(familyId);
        Optional<Family> family  = familyRepository.findById(familyId);
        // 모든 챌린지를 조회
        List<Challenge> challenges = challengeService.findRunningChallenges();

        // 가족이 다 방에 들어오지 않았을 경우 null 반환
        if (members.size() != family.get().getMaxFamilySize()) {
            return false;
        }
        DateUtil dateUtil = DateUtil.getInstance();
        // 각 회원마다 모든 챌린지에 대해 답변 생성
        int count = 0;      // member의 번호를 의미
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
                if (challenge.getChallengeType() == ChallengeType.VOICE1){
                    if (count%4 != 0){
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.VOICE2){
                    if (count%4 != 1){
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.VOICE3){
                    if (count%4 != 2){
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.VOICE4){
                    if (count%4 != 3){
                        continue;
                    }
                }

                Answer answer = getAnswer(member, challenge, strChallengeDate);
                // challengeDate를 1일 증가
                strChallengeDate = dateUtil.addDaysToStringDate(strChallengeDate,1);
                // 생성된 Answer 저장
                answerRepository.save(answer);
            }
            count++;
        }
        return true;
    }

    private Answer getAnswer(Member member, Challenge challenge, String strChallengeDate) {
        Answer answer = new Answer(); // Answer 객체 생성
        answer.setMember(member);
        answer.setChallenge(challenge);
        answer.setBubbleVisible(false);
        answer.setCreateDate(strChallengeDate);

        if (challenge.getChallengeType()== ChallengeType.NORMAL) {
            answer.setAnswerType(AnswerType.NORMAL);
        } else if (challenge.getChallengeType()== ChallengeType.GROUP_CHILD) {
            answer.setAnswerType(AnswerType.GROUP);
        } else if (challenge.getChallengeType()== ChallengeType.GROUP_PARENT) {
            answer.setAnswerType(AnswerType.GROUP);
        } else if (challenge.getChallengeType()== ChallengeType.FACETIME) {
            answer.setAnswerType(AnswerType.FACETIME);
        } else if (challenge.getChallengeType()== ChallengeType.PHOTO) {
            answer.setAnswerType(AnswerType.PHOTO);
        } else if (challenge.getChallengeType()== ChallengeType.VOICE1) {
            answer.setAnswerType(AnswerType.VOICE);
        } else if (challenge.getChallengeType()== ChallengeType.VOICE2) {
            answer.setAnswerType(AnswerType.VOICE);
        } else if (challenge.getChallengeType()== ChallengeType.VOICE3) {
            answer.setAnswerType(AnswerType.VOICE);
        } else if (challenge.getChallengeType()== ChallengeType.VOICE4) {
            answer.setAnswerType(AnswerType.VOICE);
        }
        return answer;
    }

    // challenge와 member로 answer찾기
    @Override
    @Transactional(readOnly = true)
    public boolean findIsCompleteAnswer(Challenge challenge, Member member){         // challenge와 member로 answer찾기
        Optional<Answer> answer = answerRepository.findByChallengeAndMember(challenge, member);
        return answer.map(a -> a.getAnswerContent() != null).orElse(false); // answercontent 가 존재하지 않을 경우 false, 존재할 경우 true 반환
    }

    // 챌린지와 멤버로 챌린지 답변 생성날짜 찾기
    @Override
    @Transactional(readOnly = true)
    public Long findDateByChallengeMember(Challenge challenge, Member member) throws Exception{       // answer의 createDate를 timeStamp로 바꾸기
        Optional<Answer> answer = answerRepository.findByChallengeAndMember(challenge, member);
        DateUtil dateUtil = DateUtil.getInstance();
        return dateUtil.stringToTimestamp(answer.get().getCreateDate(),"yyyy.MM.dd");
    }

    // 가족 구성원들의 답변 유무 검사
    @Override
    @Transactional(readOnly = true)
    public boolean isAnyAnswerComplete(Challenge challenge, Family family) {
        List<Member> members = family.getMembers();
        for (Member member : members) {
            Optional<Answer> answer = answerRepository.findByChallengeAndMember(challenge, member);
            if (answer.isPresent() && answer.get().getAnswerContent() != null) {
                return true;
            }
        }
        return false;
    }

    // 답변 수정
    @Override
    public Answer modifyAnswer(Long challengeId, String answerContent) throws Exception{        // 로그인한 맴버가 수정한 답변을 저장한다.
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId())
                .orElseThrow(() -> new RuntimeException("로그인한 멤버를 찾을 수 없습니다."));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("주어진 challengeId에 해당하는 챌린지를 찾을 수 없습니다."));
        Answer answer = answerRepository.findByChallengeAndMember(challenge, member)
                .orElseThrow(() -> new RuntimeException("해당 멤버가 작성한 답변을 찾을 수 없습니다."));
        Lucky lucky = luckyRepository.findByFamilyFamilyIdAndRunningTrue(member.getFamily().getFamilyId())
                .orElseThrow(() -> new RuntimeException("로그인한 멤버의 행운이를 찾을 수 없습니다"));
        if (answer.getAnswerContent() == null){
            answer.setBubbleVisible(true);
            lucky.setStatus(lucky.getStatus()+1);
            luckyRepository.save(lucky);
        }
        answer.setAnswerContent(answerContent);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        answer.setModifiedDate(LocalDateTime.now().format(formatter));
        answerRepository.save(answer);
        return answer;
    }

    // 챌린지와 멤버로 답변 찾기
    @Override
    public Optional<Answer> findAnswerByChallengeAndMember(Challenge challenge, Member member) {
        return answerRepository.findByChallengeAndMember(challenge, member);
    }

    // 답변 저장
    @Override
    public void saveAnswer(Answer answer) throws Exception {
        Member member = memberService.findMemberByLoginId();
        Long familyId = member.getFamily().getFamilyId();
        Lucky lucky = luckyService.findCurrentLucky(familyId);

        if (answer.getAnswerContent() == null){
            answer.setBubbleVisible(true);
            lucky.setStatus(lucky.getStatus()+1);
            luckyRepository.save(lucky);
        }
        answerRepository.save(answer);
    }

    // 말풍선 클릭하면 말풍선 확인 변수 끄기
    @Override
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

    // 챌린지와 멤버를 받아서 답변 리스트 반환
    @Override
    @Transactional(readOnly = true)
    public List<Answer> findAnswersByChallenges(Challenge challenge, Member member) {         // 특정 그룹 챌린지에 매핑된 answer list 찾기
        List<Challenge> groupChallenges = challengeRepository.findByChallengeNum(challenge.getChallengeNum());       // challengeNum이 같은 챌린지 찾기
        Family family = member.getFamily();
        List<Answer> allAnswers = new ArrayList<>();        // 해당 그룹질문으로 묶인 answer 가져오기
        for (Challenge relatedChallenge : groupChallenges) {
            List<Answer> answers = findAnswerByChallenge(relatedChallenge);
            allAnswers.addAll(answers);
        }
        return allAnswers.stream()
                .filter(answer -> {
                    Member answerMember = answer.getMember();
                    return answerMember != null && family.getFamilyId().equals(answerMember.getFamily().getFamilyId());
                })
                .collect(Collectors.toList());
    }


    // 공통 로직을 처리하는 메서드
    // 각 member의 정보와 특정 두 번호에 해당하는 두 챌린지 답변을 묶어 반환
    @Transactional(readOnly = true)
    public List<MemberDto> getAnswersByMember(List<Member> members, int challengeNum1, int challengeNum2, Class<? extends MemberDto> dtoClass) throws Exception {
        List<MemberDto> memberDtoList = new ArrayList<>();
        Optional<Member> currentUser = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId());
        int startChallengeNum = luckyService.findStartChallengeNum(currentUser.get().getFamily().getFamilyId()); // 이번 lucky에서 시작해야하는 challengeNum

        Challenge challenge1 = challengeRepository.findByChallengeNum(startChallengeNum + challengeNum1)
                .stream()
                .findFirst()
                .orElseThrow(() -> new Exception("Challenge " + (startChallengeNum + challengeNum1) + " not found"));

        Challenge challenge2 = challengeRepository.findByChallengeNum(startChallengeNum + challengeNum2)
                .stream()
                .findFirst()
                .orElseThrow(() -> new Exception("Challenge " + (startChallengeNum + challengeNum2) + " not found"));

        for (Member member : members) {
            Answer challenge1Answer = answerRepository.findByChallengeAndMember(challenge1, member)
                    .orElseThrow(() -> new Exception("Answer for Challenge " + (startChallengeNum + challengeNum1) + " not found"));
            Answer challenge2Answer = answerRepository.findByChallengeAndMember(challenge2, member)
                    .orElseThrow(() -> new Exception("Answer for Challenge " + (startChallengeNum + challengeNum2) + " not found"));

            String answer1 = challenge1Answer.getAnswerContent();
            String answer2 = challenge2Answer.getAnswerContent();

            memberDtoList.add(createDto(dtoClass, member, answer1, answer2));
        }
        return memberDtoList;
    }

    // DTO 생성 로직을 처리하는 메서드
    public MemberDto createDto(Class<? extends MemberDto> dtoClass, Member member, String answer1, String answer2) {
        if (dtoClass == MemberAppreciationDto.class) {
            return new MemberAppreciationDto(member, answer1, answer2);
        } else if (dtoClass == MemberYouthAnswerDto.class) {
            return new MemberYouthAnswerDto(member.getMemberImage(), answer1, answer2);
        }
        throw new IllegalArgumentException("Unsupported DTO class: " + dtoClass.getName());
    }

    @Transactional(readOnly = true)
    public List<MemberYouthAnswerDto> getYouthByMember(List<Member> members, int challengeNum1, int challengeNum2) throws Exception {
        return (List<MemberYouthAnswerDto>) (List<?>) getAnswersByMember(members, challengeNum1, challengeNum2, MemberYouthAnswerDto.class);
    }

    @Transactional(readOnly = true)
    public List<MemberAppreciationDto> getAppreciationByMember(List<Member> members, int challengeNum1, int challengeNum2) throws Exception {
        return (List<MemberAppreciationDto>) (List<?>) getAnswersByMember(members, challengeNum1, challengeNum2, MemberAppreciationDto.class);
    }

    // 각 멤버의 photo 타입의 질문에 대한 사진을 랜덤으로 반환
    @Override
    @Transactional(readOnly = true)
    public MemberPhotosAnswerDto getPhotoByMember(List<Member> members) throws Exception {
        List<Answer> memberAnswerList = new ArrayList<>();
        List<String> otherPhotos = new ArrayList<>();

        Optional<Member> currentUser = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId());
        int startChallengeNum = luckyService.findStartChallengeNum(currentUser.get().getFamily().getFamilyId());

        // 1. Challenge 19 가져오기
        Challenge challenge19 = challengeRepository.findByChallengeNum(startChallengeNum+9)
                .stream()
                .findFirst()
                .orElseThrow(() -> new Exception("Challenge 19 not found"));

        // 2. 멤버들의 Challenge 19에 대한 답변 가져오기
        for (Member member : members) {
            Answer challenge19Answer = answerRepository.findByChallengeAndMember(challenge19, member)
                    .orElseThrow(() -> new Exception("Answer for Challenge 19 not found"));
            if(challenge19Answer.getAnswerContent() != null){
                memberAnswerList.add(challenge19Answer);
            }
        }
        // 3. memberAnswerList에서 랜덤하게 하나의 answer를 선택하여 familyPhoto로 설정
        Random random = new Random();
        Answer familyPhotoAnswer = memberAnswerList.get(random.nextInt(memberAnswerList.size()));
        String familyPhoto = familyPhotoAnswer.getAnswerContent();

        // 4. type이 photo이고 challengenum이 19번이 아닌 멤버들의 answer 중에서 모든 사진 URL을 담은 리스트 생성
        List<String> allOtherPhotos = new ArrayList<>();
        int number = luckyService.findCurrentLuckyLifetime(currentUser.get().getFamily().getFamilyId());
        // 현재 진행한 challengeList
        List<Challenge> challengeList = challengeRepository.findByChallengeNumBetween(luckyService.findStartChallengeNum((currentUser.get().getFamily().getFamilyId())), number);

        for (Member member : members) {
            // 그룹챌린지를 고려한 새로운 리스트 적용
            List<Challenge> newChallengeList = challengeService.groupChallengeExceptionRemove(challengeList,member);
            for (Challenge challenge : newChallengeList) {
                if (challenge.getChallengeNum() != startChallengeNum+19){
                    Optional<Answer> answer = answerRepository.findByChallengeAndMember(challenge,member);
                    if (answer.get().getAnswerType() == PHOTO && answer.get().getAnswerContent() != null){
                        allOtherPhotos.add(answer.get().getAnswerContent());
                    }
                }
            }
        }

        // 최대 9장의 사진을 랜덤으로 고른다.
        int numPhotosToAdd = Math.min(9, allOtherPhotos.size());
        Set<Integer> chosenIndices = new HashSet<>();
        while (chosenIndices.size() < numPhotosToAdd) {
            int randomIndex = random.nextInt(allOtherPhotos.size());
            if (chosenIndices.add(randomIndex)) {
                otherPhotos.add(allOtherPhotos.get(randomIndex));
            }
        }

        return new MemberPhotosAnswerDto(familyPhoto,otherPhotos);
    }

    // facetime에 대한 answerList를 반환
    @Override
    @Transactional(readOnly = true)
    public List<String> getFacetimeAnswerList(Long luckyId){
        Optional<Member> currentUser = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId());
        Family family = currentUser.get().getFamily();
        List<Member> memberList = memberRepository.findByFamilyFamilyId(family.getFamilyId());
        int number = luckyService.findCurrentLuckyLifetime(currentUser.get().getFamily().getFamilyId());
        List<Challenge> challengeList = challengeRepository.findByChallengeNumBetween(luckyService.findStartChallengeNum((currentUser.get().getFamily().getFamilyId())), number);

        List<String> facetimeAnswerList = new ArrayList<>();
        for (Challenge challenge : challengeList){
            if(challenge.getChallengeType() == ChallengeType.FACETIME){
                for(Member member : memberList){
                    Optional<Answer> answer = answerRepository.findByChallengeAndMember(challenge,member);
                    if(answer.get().getAnswerContent() != null){
                        facetimeAnswerList.add(answer.get().getAnswerContent());
                    }
                }
                return facetimeAnswerList;
            }
        }
        return null;
    }

    // 챌린지 상세조회 중복요소 매핑
    @Override
    public ChallengeDetailsDto getChallengeDetails(Challenge challenge, Member member) throws Exception {
        boolean isComplete = this.findIsCompleteAnswer(challenge, member);
        Long challengeDate = this.findDateByChallengeMember(challenge, member);
        List<Answer> answers = this.findAnswerByChallengeAndFamily(challenge, member);

        return new ChallengeDetailsDto(challengeDate, isComplete, answers);
    }


    // 챌린지로 답변 찾기
    @Transactional(readOnly = true)
    public List<Answer> findAnswerByChallenge(Challenge challenge){
        return answerRepository.findByChallenge(challenge);
    }


    // 해당 챌린지와 매핑된 답변 중 로그인 하고 있는 맴버 가족의 답변리스트만을 반환
    @Transactional(readOnly = true)
    public List<Answer> findAnswerByChallengeAndFamily(Challenge challenge, Member member){
        Family family = member.getFamily();
        return answerRepository.findByChallenge(challenge).stream()
                .filter(answer -> {
                    Member answerMember = answer.getMember();
                    return answerMember != null && family.getFamilyId().equals(answerMember.getFamily().getFamilyId());
                })
                .collect(Collectors.toList());
    }



}
