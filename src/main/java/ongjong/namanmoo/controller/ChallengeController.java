package ongjong.namanmoo.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.*;
import ongjong.namanmoo.dto.challenge.*;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.answer.ModifyAnswerDto;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final MemberService memberService;
    private final LuckyService luckyService;
    private final AnswerService answerService;
    private final FamilyService familyService;
    private final AwsS3Service awsS3Service;
    private final SharedFileService sharedFileService;
    private final LuckyRepository luckyRepository;

    @PostMapping     // 챌린지 생성 -> 캐릭터 생성 및 답변 생성
    public ApiResponse<Void> saveChallenge(@RequestBody SaveChallengeRequest request) throws Exception {
        Long challengeDate = request.getChallengeDate();
        Long familyId = familyService.findFamilyId();
        if (!luckyService.createLucky(familyId, challengeDate) || !answerService.createAnswer(familyId, challengeDate)) {
            return new ApiResponse<>("404", "Challenge not found", null);
        }
        return new ApiResponse<>("200", "Success", null);
    }

    @GetMapping("/today")     // 오늘의 챌린지 조회
    public ApiResponse<CurrentChallengeDto> getChallenge(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        Member member = memberService.findMemberByLoginId(); // 로그인한 member
        CurrentChallengeDto currentChallenge = challengeService.findChallengesByMemberId(challengeDate, member);

        if (currentChallenge == null || currentChallenge.getChallengeInfo() == null) {
            return new ApiResponse<>("404", "Challenge not found", currentChallenge);
        }
        return new ApiResponse<>("200", "Success", currentChallenge);
    }

    @GetMapping("/list")        // 챌린지 리스트 조회 , 챌린지 리스트는 lucky가 여러개 일때를 고려하여 죽은 럭키 개수 * 30 +1 부터 챌린지가 보여져야한다.
    public ApiResponse<List<ChallengeListDto>> getChallengeList(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        List<Challenge> challenges = challengeService.findChallenges(challengeDate); //

        if (challenges == null || challenges.isEmpty()) {
            return new ApiResponse<>("failure", "No challenges found for the family", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        List<ChallengeListDto> challengeList = challenges.stream()
                .map(challenge -> {
                    boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
                    return new ChallengeListDto(challenge, isComplete);
                })
                .collect(Collectors.toList());

        return new ApiResponse<>("success", "Challenge list retrieved successfully", challengeList);
    }

    @GetMapping("/normal")      // 일반 챌린지 조회
    public ApiResponse<NormalChallengeDto> getNormalChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        ChallengeDetailsDto details = answerService.getChallengeDetails(challenge, member);
        NormalChallengeDto normalChallengeDto = new NormalChallengeDto(challenge, details.isComplete(), details.getChallengeDate(), details.getAnswers());

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        return new ApiResponse<>("success", "Challenge retrieved successfully", normalChallengeDto);      // 객체를 리스트 형태로 감싸서 반환
    }

    // 일반 챌린지 내용 수정 -> 새 내용, 수정날짜 저장
    @PostMapping("/normal")
    public  ApiResponse<ModifyAnswerDto> saveNormalAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        Long challengeId = request.getChallengeId();
        String answerContent = request.getAnswerContent();
        Answer answer = answerService.modifyAnswer(challengeId, answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse<>("200", "Success", modifyAnswerDto);
    }

    // 그룹 챌린지 조회
    @GetMapping("/group")
    public ApiResponse<GroupChallengeDto> getGroupChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        Long challengeDate = answerService.findDateByChallengeMember(challenge, member);
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        List<Answer> allAnswers = answerService.findAnswersByChallenges(challenge, member);     // 특정 그룹 챌린지에 매핑된 answer list 찾기

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        GroupChallengeDto groupChallengeDto = challengeService.filterChallengesByMemberRole(challenge, challengeDate, isComplete, allAnswers);
        return new ApiResponse<>("200", "Challenge retrieved successfully", groupChallengeDto);

    }

    // 그룹 챌린지 답변 수정
    @PostMapping("/group")
    public  ApiResponse<ModifyAnswerDto> saveGroupAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        Long challengeId = request.getChallengeId();
        String answerContent = request.getAnswerContent();
        Answer answer = answerService.modifyAnswer(challengeId, answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse<>("200", "Success", modifyAnswerDto);
    }

    // 사진 챌린지 조회
    @GetMapping("/photo")
    public ApiResponse<PhotoChallengeDto> getPhotoChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        return getPhotoOrVoiceChallenge(challengeId, PhotoChallengeDto.class);
    }

    // 사진 챌린지 수정
    @PostMapping("/photo")
    public ApiResponse<Map<String, String>> savePhotoAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestPart("answer") MultipartFile answerFile) throws Exception {
        // challengeId RequestParam으로 변경해서 테스트

        if (answerFile == null || answerFile.isEmpty()) {
            return new ApiResponse<>("400", "Answer file is missing", null);
        }

        // S3에 파일 업로드
        String uploadImageUrl = awsS3Service.uploadFile(answerFile);

        // Answer 업데이트
        Answer answer = answerService.modifyAnswer(challengeId, uploadImageUrl);

        // Map 형태로 답변 URL 반환
        Map<String, String> responseData = new HashMap<>();
        responseData.put("answer", uploadImageUrl);

        return new ApiResponse<>("200", "Success", responseData);
    }

    // 화상 통화 챌린지 조회
    @GetMapping("/face")
    public ApiResponse<Object> getFaceChallenge(
            @RequestParam("challengeId") Long challengeId) throws Exception {

        // 챌린지 조회
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }
        // 가족 초대 코드 조회 (멤버를 통해 가족 정보를 가져온 후 초대 코드 획득)
        Member member = memberService.findMemberByLoginId();

        // 멤버와 챌린지를 통해 timestamp 조회
        Long timestamp = answerService.findDateByChallengeMember(challenge, member);

        Family family = member.getFamily();
        String inviteCode = family != null ? family.getInviteCode() : null;
        assert family != null;

        // isComplete 계산 로직
        boolean isComplete = answerService.isAnyAnswerComplete(challenge, family);

        // 화상 통화 챌린지 정보 DTO 생성
        FaceChallengeDto challengeDto = new FaceChallengeDto(challenge, timestamp, isComplete, inviteCode);

        return new ApiResponse<>("200", "Success", challengeDto);
    }

    // 화상 통화 챌린지 결과 저장
    @PostMapping("/face")
    public ApiResponse<Map<String, String>> saveFaceTimeAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestPart("answer") MultipartFile answerFile) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);

        if (answerFile == null || answerFile.isEmpty()) {
            return new ApiResponse<>("400", "Answer file is missing", null);
        }

        FileType fileType;
        if (answerFile.getContentType().startsWith("image/")) {
            fileType = FileType.IMAGE;
            Map<String, String> response = sharedFileService.uploadImageFile(challenge, answerFile, fileType);
            return new ApiResponse<>("200", response.get("message"), response);
        } else if (answerFile.getContentType().startsWith("video/")) {
            fileType = FileType.VIDEO;

            // S3에 파일 업로드 및 URL 저장
            String uploadedUrl = awsS3Service.uploadFile(answerFile);

            // Answer 업데이트
            Answer answer = answerService.modifyAnswer(challengeId, uploadedUrl);
            Map<String, String> response = new HashMap<>();

            response.put("url", uploadedUrl);
            response.put("message", "Video uploaded successfully");
            return new ApiResponse<>("200", response.get("message"), response);
        } else {
            return new ApiResponse<>("400", "Invalid file type: " + answerFile.getContentType(), null);
        }
    }

    // 화상 통화 챌린지 결과 조회
    @GetMapping("/face/result")
    public ApiResponse<Map<String, List<String>>> getFaceTimeAnswer(
            @RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId();
        Family family = member.getFamily();

        if (family == null) {
            return new ApiResponse<>("404", "Family not found for the current member", null);
        }

        Optional<Lucky> lucky = luckyRepository.findByFamilyFamilyIdAndRunningTrue(family.getFamilyId());

        if (!lucky.isPresent()) {
            return new ApiResponse<>("404", "Lucky not found for the provided challengeId in any family member", null);
        }

        // 응답 데이터 생성
        Map<String, List<String>> results = sharedFileService.getChallengeResults(challenge.getChallengeNum(), lucky.get().getLuckyId());

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        return new ApiResponse<>("200", "Challenge results fetched successfully", results);
    }

    // 음성 챌린지 조회
    @GetMapping("/voice")
    public ApiResponse<VoiceChallengeDto> getVoiceChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        return getPhotoOrVoiceChallenge(challengeId, VoiceChallengeDto.class);
    }

    // 음성 챌린지 수정 (바뀔 예정 있으므로 중복 처리 아직 안함)
    @PostMapping("/voice")
    public ApiResponse<Map<String, String>> saveVoiceAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestPart("answer") MultipartFile answerFile) throws Exception {
        // challengeId RequestParam으로 변경해서 테스트

        if (answerFile == null || answerFile.isEmpty()) {
            return new ApiResponse<>("400", "Answer file is missing", null);
        }

        // S3에 파일 업로드
        String uploadVoiceUrl = awsS3Service.uploadFile(answerFile);

        // Answer 업데이트
        Answer answer = answerService.modifyAnswer(challengeId, uploadVoiceUrl);

        // Map 형태로 답변 URL 반환
        Map<String, String> responseData = new HashMap<>();
        responseData.put("answer", uploadVoiceUrl);


        return new ApiResponse<>("200", "Success", responseData);
    }

    // 사진, 음성 챌린지 중복 로직 처리
    private <T> ApiResponse<T> getPhotoOrVoiceChallenge(Long challengeId, Class<T> dtoClass) throws Exception {
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        ChallengeDetailsDto details = answerService.getChallengeDetails(challenge, member);

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        T challengeDto = dtoClass.getConstructor(Challenge.class, boolean.class, Long.class, List.class)
                .newInstance(challenge, details.isComplete(), details.getChallengeDate(), details.getAnswers());

        return new ApiResponse<>("200", "Challenge retrieved successfully", challengeDto);
    }

}
