package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.*;
import ongjong.namanmoo.domain.challenge.*;
import ongjong.namanmoo.dto.challenge.ChallengeDetailsDto;
import ongjong.namanmoo.dto.recap.*;
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

    @Override
    public boolean createAnswer(Long familyId, Long challengeDate) throws Exception {

        // 가족 ID로 해당 가족의 회원들을 조회 (멤버 오름차순으로 나오도록 수정)
        List<Member> members = memberRepository.findByFamilyFamilyIdOrderByMemberIdAsc(familyId);
        Optional<Family> family  = familyRepository.findById(familyId);

        if (family.isEmpty()) {
            throw new Exception("Family not found");
        }

        DateUtil dateUtil = DateUtil.getInstance();

        // 주어진 challengeDate와 familyId로 기존 답변 조회
        List<Answer> existingAnswer = answerRepository.findByCreateDateAndMemberFamily(dateUtil.timestampToString(challengeDate), family.get());

        // 이미 존재하는 답변이 있는 경우 예외 발생
        if (!existingAnswer.isEmpty()) {
            throw new Exception("Answer already exists for the given challenge date");
        }

        // 모든 챌린지를 조회
        List<Challenge> challenges = challengeService.findRunningChallenges();

        // 가족이 다 방에 들어오지 않았을 경우 false 반환
        if (members.size() != family.get().getMaxFamilySize()) {
            return false;
        }

        // 각 회원마다 모든 챌린지에 대해 답변 생성
        int count = 0;  // member의 번호를 의미
        for (Member member : members) {
            String strChallengeDate = dateUtil.timestampToString(challengeDate);  // timestamp인 challengedate를 string "yyyy.MM.dd" 으로 변환
            for (Challenge challenge : challenges) {  // 현재 챌린지의 개수 만큼 answer 생성 -> 챌린지의 개수가 30개가 넘었을 경우 stop
                if (challenge.getChallengeType() == ChallengeType.GROUP_PARENT) {  // 만약 member가 아빠 일경우, challenge가 CGROUP이면 ANSWER 생성 X
                    if (member.getRole().equals("아들") || member.getRole().equals("딸")) {
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.GROUP_CHILD) {
                    if (member.getRole().equals("아빠") || member.getRole().equals("엄마")) {
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.VOICE1) {
                    if (count % 4 != 0) {
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.VOICE2) {
                    if (count % 4 != 1) {
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.VOICE3) {
                    if (count % 4 != 2) {
                        continue;
                    }
                }
                if (challenge.getChallengeType() == ChallengeType.VOICE4) {
                    if (count % 4 != 3) {
                        continue;
                    }
                }

                Answer answer = getAnswer(member, challenge, strChallengeDate);
                // challengeDate를 1일 증가
                strChallengeDate = dateUtil.addDaysToStringDate(strChallengeDate, 1);
                // 생성된 Answer 저장
                answerRepository.save(answer);
            }
            count++;
        }
        return true;
    }

    private Answer getAnswer(Member member, Challenge challenge, String strChallengeDate) {
        // EnumSet으로 코드를 타입을 묶어 중복 코드 제거
        EnumSet<ChallengeType> voiceTypes = EnumSet.of(ChallengeType.VOICE1, ChallengeType.VOICE2, ChallengeType.VOICE3, ChallengeType.VOICE4);
        EnumSet<ChallengeType> groupTypes = EnumSet.of(ChallengeType.GROUP_CHILD,ChallengeType.GROUP_PARENT);
        Answer answer = new Answer(); // Answer 객체 생성
        answer.setMember(member);
        answer.setChallenge(challenge);
        answer.setBubbleVisible(false);
        answer.setCreateDate(strChallengeDate);

        if (challenge.getChallengeType()== ChallengeType.NORMAL) {
            answer.setAnswerType(AnswerType.NORMAL);
        } else if (groupTypes.contains(challenge.getChallengeType())) {
            answer.setAnswerType(AnswerType.GROUP);
        } else if (challenge.getChallengeType()== ChallengeType.FACETIME) {
            answer.setAnswerType(AnswerType.FACETIME);
        } else if (challenge.getChallengeType()== ChallengeType.PHOTO) {
            answer.setAnswerType(AnswerType.PHOTO);
        } else if (voiceTypes.contains(challenge.getChallengeType())) {
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

        // answer 값을 가져가기 전에 answer가 존재하는지 확인
        return answer.map(value -> dateUtil.stringToTimestamp(value.getCreateDate(), "yyyy.MM.dd")).orElse(null);
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
    public List<MemberDto> getAnswersByMember(Long luckyId, int challengeNum1, int challengeNum2, Class<? extends MemberDto> dtoClass) throws Exception {
        // luckyId로 Lucky 객체 가져오기
        Lucky lucky = luckyRepository.getLuckyByLuckyId(luckyId).orElseThrow(() -> new Exception("luckyId not found"));

        // 시작 날짜와 종료 날짜 계산
        String startDate = lucky.getChallengeStartDate();
        String endDate = DateUtil.getInstance().addDaysToStringDate(startDate, lucky.getLifetime().getDays());

        // luckyId로 가족 멤버 목록 가져오기
        List<Member> members = memberRepository.findByFamilyFamilyId(lucky.getFamily().getFamilyId());

        // Lucky 기간 동안의 모든 답변 가져오기
        List<Answer> answersWithinDateRange = answerRepository.findByMemberFamilyAndCreateDateBetween(lucky.getFamily(), startDate, endDate);

        // challengeNum1과 challengeNum2에 해당하는 날짜 계산
        String dateCHN1 = DateUtil.getInstance().addDaysToStringDate(lucky.getChallengeStartDate(), challengeNum1 - 1);
        String dateCHN2 = DateUtil.getInstance().addDaysToStringDate(lucky.getChallengeStartDate(), challengeNum2 - 1);

        // challengeNum1과 challengeNum2에 해당하는 답변 필터링
        List<Answer> challengeNum1Answers = answersWithinDateRange.stream()
                .filter(a -> dateCHN1.equals(a.getCreateDate()))
                .toList();
        List<Answer> challengeNum2Answers = answersWithinDateRange.stream()
                .filter(a -> dateCHN2.equals(a.getCreateDate()))
                .toList();

        List<MemberDto> memberDtoList = new ArrayList<>();

        // 각 멤버에 대해 DTO를 생성하여 리스트에 추가
        for (Member member : members) {
            Answer challengeNum1Answer = challengeNum1Answers.stream()
                    .filter(a -> a.getMember().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Answer for Challenge " + challengeNum1 + " not found"));

            Answer challengeNum2Answer = challengeNum2Answers.stream()
                    .filter(a -> a.getMember().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Answer for Challenge " + challengeNum2 + " not found"));

            String answer1 = challengeNum1Answer.getAnswerContent();
            String answer2 = challengeNum2Answer.getAnswerContent();

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
    public List<MemberYouthAnswerDto> getYouthByMember(Long luckyId, int challengeNum1, int challengeNum2) throws Exception {
        return (List<MemberYouthAnswerDto>) (List<?>) getAnswersByMember(luckyId, challengeNum1, challengeNum2, MemberYouthAnswerDto.class);
    }

    @Transactional(readOnly = true)
    public List<MemberAppreciationDto> getAppreciationByMember(Long luckyId, int challengeNum1, int challengeNum2) throws Exception {
        return (List<MemberAppreciationDto>) (List<?>) getAnswersByMember(luckyId, challengeNum1, challengeNum2, MemberAppreciationDto.class);
    }

    // 각 멤버의 photo 타입의 질문에 대한 사진을 랜덤으로 반환
    @Override
    @Transactional(readOnly = true)
    public MemberPhotosAnswerDto getPhotos(Long luckyId) throws Exception {
        // 현재 로그인한 사용자 가져오기
        Optional<Member> currentUser = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId());
        if(currentUser.isEmpty()) {
            throw new Exception("Current user not found");
        }

        // luckyId로 Lucky 객체 가져오기
        Lucky lucky = luckyRepository.getLuckyByLuckyId(luckyId).orElseThrow(() -> new Exception("Lucky ID not found"));

        // luckyId로 가족 멤버 목록 가져오기
        List<Member> members = memberRepository.findByFamilyFamilyId(lucky.getFamily().getFamilyId());

        // 시작 날짜와 종료 날짜 계산
        String startDate = lucky.getChallengeStartDate();
        String challenge19Date = DateUtil.getInstance().addDaysToStringDate(startDate, 18);
        String endDate = DateUtil.getInstance().addDaysToStringDate(startDate, lucky.getLifetime().getDays());

        // Lucky 기간 동안의 모든 답변 가져오기
        List<Answer> answersWithinDateRange = answerRepository.findByMemberFamilyAndCreateDateBetween(lucky.getFamily(), startDate, endDate);

        // Challenge 19에 대한 답변 가져오기
        List<Answer> challenge19Answers = answerRepository.findByCreateDateAndMemberFamily(challenge19Date, lucky.getFamily());
        List<Answer> memberAnswerList = challenge19Answers.stream()
                .filter(answer -> members.contains(answer.getMember()))
                .toList();

        // 가족 사진을 위한 랜덤한 답변 선택
        Random random = new Random();
        Answer familyPhotoAnswer = memberAnswerList.get(random.nextInt(memberAnswerList.size()));
        String familyPhoto = familyPhotoAnswer.getAnswerContent();

        // Challenge 19에 대한 답변을 제외한 다른 사진 답변의 URL을 수집하고 null 값 제거
        List<String> allOtherPhotos = answersWithinDateRange.stream()
                .filter(answer -> !challenge19Answers.contains(answer))
                .filter(answer -> answer != familyPhotoAnswer && answer.getAnswerType() == AnswerType.PHOTO)
                .map(Answer::getAnswerContent)
                .filter(Objects::nonNull)
                .toList();

        // 최대 9장의 다른 사진을 랜덤으로 선택
        List<String> otherPhotos = new ArrayList<>();
        int numPhotosToAdd = Math.min(9, allOtherPhotos.size());
        Set<Integer> chosenIndices = new HashSet<>();
        while (chosenIndices.size() < numPhotosToAdd) {
            int randomIndex = random.nextInt(allOtherPhotos.size());
            if (chosenIndices.add(randomIndex)) {
                otherPhotos.add(allOtherPhotos.get(randomIndex));
            }
        }

        // MemberPhotosAnswerDto 객체를 생성하여 반환
        return new MemberPhotosAnswerDto(familyPhoto, otherPhotos);
    }

    // facetime에 대한 answerList를 반환
    @Override
    @Transactional(readOnly = true)
    public MemberFacetimeDto getFacetimeAnswerList(Long luckyId) throws Exception {
        Optional<Member> currentUser = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId());
        Lucky lucky = luckyRepository.getLuckyByLuckyId(luckyId).orElseThrow(() -> new Exception("luckyId not found"));
        Family family = lucky.getFamily();
        List<Member> memberList = memberRepository.findByFamilyFamilyId(family.getFamilyId());

        // Lucky의 시작 날짜와 기간을 기반으로 해당 기간 동안의 모든 답변을 가져오기
        List<Answer> answersWithinDateRange = findAnswersWithinDateRange(family, lucky.getChallengeStartDate(), lucky.getLifetime().getDays());

        // FACETIME 답변만 필터링
        List<Answer> facetimeAnswers = answersWithinDateRange.stream()
                .filter(answer -> answer.getChallenge().getChallengeType() == ChallengeType.FACETIME)
                .toList();

        // 가져온 FACETIME 답변이 없으면 null 반환
        if (facetimeAnswers.isEmpty()) {
            return null;
        }

        // ChallengeId 별로 그룹화
        Map<Long, List<Answer>> answersGroupedByChallengeId = facetimeAnswers.stream()
                .collect(Collectors.groupingBy(answer -> answer.getChallenge().getChallengeId()));

        // 그룹 중 무작위로 하나의 ChallengeId 선택
        List<Long> challengeIds = new ArrayList<>(answersGroupedByChallengeId.keySet());
        Collections.shuffle(challengeIds);
        Long randomChallengeId = challengeIds.get(0);

        // 선택된 ChallengeId에 해당하는 답변 리스트
        List<Answer> selectedAnswers = answersGroupedByChallengeId.get(randomChallengeId);

        List<String> facetimeAnswerList = new ArrayList<>();  // FACETIME 답변 내용을 담을 리스트
        String challengeDate = selectedAnswers.get(0).getCreateDate();  // 임의로 선택된 FACETIME 답변의 생성 날짜

        // 선택된 ChallengeId에 해당하는 모든 답변 내용 추가
        for (Answer answer : selectedAnswers) {
            facetimeAnswerList.add(answer.getAnswerContent());
        }

        // 챌린지 날짜를 타임스탬프로 변환
        long challengeTimestamp = DateUtil.getInstance().stringToTimestamp(challengeDate, DateUtil.FORMAT_4);
        return new MemberFacetimeDto(challengeTimestamp, facetimeAnswerList);  // 만들어진 답변 리스트와 챌린지 날짜를 멤버에 담아서 반환
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

    // 특정 기간 동안의 답변을 가져오는 함수
    public List<Answer> findAnswersWithinDateRange(Family family, String startDate, int days) {
        String endDate = DateUtil.getInstance().addDaysToStringDate(startDate, days);
        return answerRepository.findByMemberFamilyAndCreateDateBetween(family, startDate, endDate);
    }

}
