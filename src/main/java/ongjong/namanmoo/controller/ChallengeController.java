package ongjong.namanmoo.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.*;
import ongjong.namanmoo.domain.challenge.ChallengeType;
import ongjong.namanmoo.dto.challenge.*;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.answer.ModifyAnswerDto;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

    @PostMapping     // 챌린지 생성 -> 캐릭터 생성 및 답변 생성
    public ApiResponse<Void> saveChallenge(@RequestBody SaveChallengeRequest request) throws Exception {
        Long challengeDate = request.getChallengeDate();
        Long familyId = familyService.findFamilyId();
        if (!luckyService.createLucky(familyId, challengeDate) || !answerService.createAnswer(familyId, challengeDate)) {
            return new ApiResponse<>("404", "Challenge not found", null);
        }
        return new ApiResponse<>("201", "Challenge created successfully", null);
    }

    @GetMapping("/today")     // 오늘의 챌린지 조회
    public ApiResponse<CurrentChallengeDto> getChallenge(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        Member member = memberService.findMemberByLoginId(); // 로그인한 member
        CurrentChallengeDto currentChallenge = challengeService.findChallengesByMemberId(challengeDate, member);

        if (currentChallenge == null || currentChallenge.getChallengeInfo() == null) {
            return new ApiResponse<>("404", "Challenge not found", currentChallenge);
        }
        return new ApiResponse<>("200", "Challenge found successfully", currentChallenge);
    }

    @GetMapping("/list")        // 챌린지 리스트 조회 , 챌린지 리스트는 lucky가 여러개 일때를 고려하여 죽은 럭키 개수 * 30 +1 부터 챌린지가 보여져야한다.
    public ApiResponse<List<ChallengeListDto>> getChallengeList(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        List<Challenge> challenges = challengeService.findChallenges(challengeDate); //

        if (challenges == null || challenges.isEmpty()) {
            return new ApiResponse<>("400", "No challenges found for the family", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        List<ChallengeListDto> challengeList = challenges.stream()
                .map(challenge -> {
                    boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
                    return new ChallengeListDto(challenge, isComplete);
                })
                .collect(Collectors.toList());

        return new ApiResponse<>("200", "Challenge list found successfully", challengeList);
    }

    @GetMapping("/normal")      // 일반 챌린지 조회
    public ApiResponse<NormalChallengeDto> getNormalChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.NORMAL)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        ChallengeDetailsDto details = answerService.getChallengeDetails(challenge, member);
        NormalChallengeDto normalChallengeDto = new NormalChallengeDto(challenge, details.isComplete(), details.getChallengeDate(), details.getAnswers());

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        return new ApiResponse<>("200", "Normal challenge found successfully", normalChallengeDto);       // 객체를 리스트 형태로 감싸서 반환
    }

    // 일반 챌린지 내용 수정 -> 새 내용, 수정날짜 저장
    @PostMapping("/normal")
    public  ApiResponse<ModifyAnswerDto> saveNormalAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        Long challengeId = request.getChallengeId();
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.NORMAL)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }
        String answerContent = request.getAnswerContent();
        Answer answer = answerService.modifyAnswer(challengeId, answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse<>("200", "Normal Challenge modified successfully", modifyAnswerDto);
    }

    // 그룹 챌린지 조회
    @GetMapping("/group")
    public ApiResponse<GroupChallengeDto> getGroupChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.GROUP_CHILD) && !challenge.getChallengeType().equals(ChallengeType.GROUP_PARENT)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        Long challengeDate = answerService.findDateByChallengeMember(challenge, member);
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        List<Answer> allAnswers = answerService.findAnswersByChallenges(challenge, member);     // 특정 그룹 챌린지에 매핑된 answer list 찾기

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        GroupChallengeDto groupChallengeDto = challengeService.filterChallengesByMemberRole(challenge, challengeDate, isComplete, allAnswers);
        return new ApiResponse<>("200", "Group Challenge found successfully", groupChallengeDto);

    }

    // 그룹 챌린지 답변 수정
    @PostMapping("/group")
    public  ApiResponse<ModifyAnswerDto> saveGroupAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        Long challengeId = request.getChallengeId();
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.GROUP_CHILD) && !challenge.getChallengeType().equals(ChallengeType.GROUP_PARENT)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }
        String answerContent = request.getAnswerContent();
        Answer answer = answerService.modifyAnswer(challengeId, answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse<>("200", "Group Challenge modified successfully", modifyAnswerDto);
    }

    // 사진 챌린지 조회
    @GetMapping("/photo")
    public ApiResponse<PhotoChallengeDto> getPhotoChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.PHOTO)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        ChallengeDetailsDto details = answerService.getChallengeDetails(challenge, member);

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());
        PhotoChallengeDto photoChallengeDto = new PhotoChallengeDto(challenge, details.isComplete(), details.getChallengeDate(), details.getAnswers());

        return new ApiResponse<>("200", "Photo challenge found successfully", photoChallengeDto);
    }

    // 사진 챌린지 수정
    @PostMapping("/photo")
    public ApiResponse<Map<String, String>> savePhotoAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestPart("answer") MultipartFile answerFile) throws Exception {
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.PHOTO)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }

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

        return new ApiResponse<>("200", "Photo Challenge modified successfully", responseData);
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
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.FACETIME)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
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

        return new ApiResponse<>("200", "FaceTime Challenge found successfully", challengeDto);
    }

    // 화상 통화 챌린지 결과 저장
    @PostMapping("/face")
    public ApiResponse<Map<String, String>> saveFaceTimeAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestPart("answer") MultipartFile answerFile) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.FACETIME)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }
        if (answerFile == null || answerFile.isEmpty()) {
            return new ApiResponse<>("400", "Answer file is missing", null);
        }
        Member member = memberService.findMemberByLoginId();
        Family family = member.getFamily();
        if (family == null) {
            return new ApiResponse<>("404", "Family not found for the current member", null);
        }
        Lucky lucky = luckyService.findCurrentLucky(family.getFamilyId());

        FileType fileType;
        if (Objects.requireNonNull(answerFile.getContentType()).startsWith("image/")) {
            fileType = FileType.IMAGE;
            Map<String, String> response = sharedFileService.uploadImageFile(challenge, answerFile, fileType);
            return new ApiResponse<>("200", response.get("message"), response);
        } else if (answerFile.getContentType().startsWith("video/")) {
            fileType = FileType.VIDEO;

            // S3에 파일 업로드 및 URL 저장
            String uploadedUrl = awsS3Service.uploadFile(answerFile);

            // Answer 업데이트
            Answer answer = answerService.modifyAnswer(challengeId, uploadedUrl);

            // 그룹별 4개의 이미지가 모였는지 확인 및 병합
            sharedFileService.checkAndMergeImages(challenge.getChallengeNum(), lucky);

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
    public ApiResponse<Map<Integer, List<String>>> getFaceTimeAnswer(
            @RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.FACETIME)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId();
        Family family = member.getFamily();
        if (family == null) {
            return new ApiResponse<>("404", "Family not found for the current member", null);
        }
        Lucky lucky = luckyService.findCurrentLucky(family.getFamilyId());

        // 응답 데이터 생성
        Map<Integer, List<String>> results = sharedFileService.getFaceChallengeResults(challenge.getChallengeNum(), lucky.getLuckyId());

        return new ApiResponse<>("200", "FaceTime Challenge results found successfully", results);
    }

    // 음성 챌린지 조회
    @GetMapping("/voice")
    public ApiResponse<VoiceChallengeDto> getVoiceChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge with provided challengeId not found", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.VOICE1) && !challenge.getChallengeType().equals(ChallengeType.VOICE2) && !challenge.getChallengeType().equals(ChallengeType.VOICE3) && !challenge.getChallengeType().equals(ChallengeType.VOICE4)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        ChallengeDetailsDto details = answerService.getChallengeDetails(challenge, member);

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());
        VoiceChallengeDto voiceChallengeDto = new VoiceChallengeDto(challenge, details.isComplete(), details.getChallengeDate(), details.getAnswers());

        return new ApiResponse<>("200", "Voice Challenge found successfully", voiceChallengeDto);
    }

    // 음성 챌린지 수정 (바뀔 예정 있으므로 중복 처리 아직 안함)
    @PostMapping("/voice")
    public ApiResponse<Map<String, String>> saveVoiceAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestPart("answer") MultipartFile answerFile) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge with provided challengeId not found", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유0형에 맞지 않으면 오류 반환
        if (!challenge.getChallengeType().equals(ChallengeType.VOICE1) && !challenge.getChallengeType().equals(ChallengeType.VOICE2) && !challenge.getChallengeType().equals(ChallengeType.VOICE3) && !challenge.getChallengeType().equals(ChallengeType.VOICE4)) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }
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


        return new ApiResponse<>("200", "Voice Challenge modified successfully", responseData);
    }

}
