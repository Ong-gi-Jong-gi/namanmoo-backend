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
import ongjong.namanmoo.dto.lucky.CurrentLuckyDto;
import ongjong.namanmoo.dto.openAI.TranscriptionRequest;
import ongjong.namanmoo.dto.openAI.WhisperTranscriptionResponse;
import ongjong.namanmoo.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    private final OpenAIClientService openAIClientService;

    @PostMapping // 챌린지 생성 -> 캐릭터 생성 및 답변 생성
    @Transactional
    public ApiResponse<Void> saveChallenge(@RequestBody SaveChallengeRequest request) {
        Long challengeDate = request.getChallengeDate();
        Long familyId = familyService.findFamilyId();

        try {
            // 1. 캐릭터 생성 시도
            boolean isLuckyCreated = luckyService.createLucky(familyId, challengeDate);
            if (!isLuckyCreated) {
                throw new RuntimeException("Lucky creation failed");
            }

            // 2. 답변 생성 시도
            boolean isAnswerCreated = answerService.createAnswer(familyId, challengeDate);
            if (!isAnswerCreated) {
                throw new RuntimeException("Answer creation failed");
            }

            // 성공적으로 생성된 경우
            return new ApiResponse<>("200", "Challenge created successfully", null);

        } catch (Exception e) {
            // 예외 발생 시 트랜잭션 롤백
            log.error("Challenge creation failed", e);
            return new ApiResponse<>("404", e.getMessage(), null);
        }
    }

    // 현재 진행중인 챌린지 시작 날짜를 반환
    @GetMapping("/startDate")
    public ApiResponse<CurrentLuckyDto> getChallengeStartDate(){
        Long familyId = familyService.findFamilyId();
        Lucky lucky = luckyService.findCurrentLucky(familyId);
        if (lucky == null) {
            return new ApiResponse<>("404", "Lucky Not Found", null);
        }
        CurrentLuckyDto currentLuckyDto = new CurrentLuckyDto(DateUtil.getInstance().stringToTimestamp(lucky.getChallengeStartDate(),DateUtil.FORMAT_4));
        return new ApiResponse<>("200", "Success", currentLuckyDto);
    }

    // 오늘의 챌린지 조회
    @GetMapping("/today")
    public ApiResponse<CurrentChallengeDto> getChallenge(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        if(String.valueOf(challengeDate).length() != 13){
            return new ApiResponse<>("404", "Challenge date must be a 13-number", null);
        }
        Member member = memberService.findMemberByLoginId(); // 로그인한 member
        CurrentChallengeDto currentChallenge = challengeService.findChallengesByMemberId(challengeDate, member);

        if (currentChallenge == null || currentChallenge.getChallengeInfo() == null) {
            return new ApiResponse<>("404", "Challenge not found", currentChallenge);
        }
        return new ApiResponse<>("200", "Challenge found successfully", currentChallenge);
    }

    // 챌린지 리스트 조회
    @GetMapping("/list")        // 챌린지 리스트는 lucky가 여러개 일때를 고려하여 죽은 럭키 개수 * 30 +1 부터 챌린지가 보여져야한다.
    public ApiResponse<List<ChallengeListDto>> getChallengeList(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        if(String.valueOf(challengeDate).length() != 13){
            return new ApiResponse<>("404", "Challenge date must be a 13-number", null);
        }

        List<Challenge> challenges = challengeService.findChallenges(challengeDate);

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

    // 일반 챌린지 조회
    @GetMapping("/normal")
    public ApiResponse<NormalChallengeDto> getNormalChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.NORMAL);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        ChallengeDetailsDto details = answerService.getChallengeDetails(challenge, member);
        NormalChallengeDto normalChallengeDto = new NormalChallengeDto(challenge, details.isComplete(), details.getChallengeDate(), details.getAnswers());

        // 챌린지 조회 시 조회수 증가
        Lucky matchedLucky = luckyService.findMatchingLucky(challengeId, member);
        if (matchedLucky != null && matchedLucky.isRunning()){
            luckyService.increaseChallengeViews(matchedLucky.getLuckyId(), challenge.getChallengeNum());
        }

        return new ApiResponse<>("200", "Normal challenge found successfully", normalChallengeDto);
    }

    // 일반 챌린지 내용 수정 -> 새 내용, 수정날짜 저장
    @PostMapping("/normal")
    public ApiResponse<ModifyAnswerDto> saveNormalAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(request.getChallengeId(), ChallengeType.NORMAL);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        String answerContent = request.getAnswerContent();
        Answer answer = answerService.modifyAnswer(challenge.getChallengeId(), answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse<>("200", "Normal Challenge modified successfully", modifyAnswerDto);
    }

    // 그룹 챌린지 조회
    @GetMapping("/group")
    public ApiResponse<GroupChallengeDto> getGroupChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.GROUP_CHILD, ChallengeType.GROUP_PARENT);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        Long challengeDate = answerService.findDateByChallengeMember(challenge, member);
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        List<Answer> allAnswers = answerService.findAnswersByChallenges(challenge, member);     // 특정 그룹 챌린지에 매핑된 answer list 찾기

        // 챌린지 조회 시 조회수 증가
        Lucky matchedLucky = luckyService.findMatchingLucky(challengeId, member);
        if (matchedLucky != null && matchedLucky.isRunning()){
            luckyService.increaseChallengeViews(matchedLucky.getLuckyId(), challenge.getChallengeNum());
        }

        GroupChallengeDto groupChallengeDto = challengeService.filterChallengesByMemberRole(challenge, challengeDate, isComplete, allAnswers);
        return new ApiResponse<>("200", "Group Challenge found successfully", groupChallengeDto);
    }

    // 그룹 챌린지 답변 수정
    @PostMapping("/group")
    public ApiResponse<ModifyAnswerDto> saveGroupAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(request.getChallengeId(), ChallengeType.GROUP_CHILD, ChallengeType.GROUP_PARENT);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        String answerContent = request.getAnswerContent();
        Answer answer = answerService.modifyAnswer(challenge.getChallengeId(), answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse<>("200", "Group Challenge modified successfully", modifyAnswerDto);
    }

    // 사진 챌린지 조회
    @GetMapping("/photo")
    public ApiResponse<PhotoChallengeDto> getPhotoChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.PHOTO);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        Long challengeDate = answerService.findDateByChallengeMember(challenge, member);
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        List<Answer> allAnswers = answerService.findAnswersByChallenges(challenge, member);     // 특정 사진 챌린지에 매핑된 answer list 찾기

        // 챌린지 조회 시 조회수 증가
        Lucky matchedLucky = luckyService.findMatchingLucky(challengeId, member);
        if (matchedLucky != null && matchedLucky.isRunning()){
            luckyService.increaseChallengeViews(matchedLucky.getLuckyId(), challenge.getChallengeNum());
        }

        PhotoChallengeDto photoChallengeDto = new PhotoChallengeDto(challenge, isComplete, challengeDate, allAnswers);
        return new ApiResponse<>("200", "Photo Challenge found successfully", photoChallengeDto);
    }

    // 사진 챌린지 답변 저장
    @PostMapping("/photo")
    public ApiResponse<ModifyAnswerDto> savePhotoAnswer(@RequestParam("challengeId") Long challengeId,
                                                        @RequestParam("answer") MultipartFile answerFile) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.PHOTO);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        ApiResponse<Void> fileResponse = validateFile(answerFile);
        if (!fileResponse.getStatus().equals("200")) {
            return new ApiResponse<>(fileResponse.getStatus(), fileResponse.getMessage(), null);
        }

        String fileUrl = awsS3Service.uploadFile(answerFile);
        Answer answer = answerService.modifyAnswer(challenge.getChallengeId(), fileUrl);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse<>("200", "Photo Challenge modified successfully", modifyAnswerDto);
    }

    // 화상 통화 챌린지 조회
    @GetMapping("/face")
    public ApiResponse<Object> getFaceChallenge(
            @RequestParam("challengeId") Long challengeId) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.FACETIME);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        // 가족 초대 코드 조회 (멤버를 통해 가족 정보를 가져온 후 초대 코드 획득)
        Member member = memberService.findMemberByLoginId();

        // 멤버와 챌린지를 통해 timestamp 조회
        Long timestamp = answerService.findDateByChallengeMember(challenge, member);

        Family family = member.getFamily();
        String inviteCode = family != null ? family.getInviteCode() : null;
        assert family != null;

        // isComplete 계산 로직
        boolean isComplete = answerService.isAnyAnswerModified(challenge, family);

        // 화상 통화 챌린지 정보 DTO 생성
        FaceChallengeDto challengeDto = new FaceChallengeDto(challenge, timestamp, isComplete, inviteCode);

        return new ApiResponse<>("200", "FaceTime Challenge found successfully", challengeDto);
    }

//    // 화상 통화 챌린지 결과 저장
//    @PostMapping("/face")
//    public ApiResponse<Map<String, String>> saveFaceTimeAnswer(
//            @RequestParam("challengeId") Long challengeId,
//            @RequestPart("answer") MultipartFile answerFile) throws Exception {
//
//        // Challenge 검증
//        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.FACETIME);
//        if (!challengeResponse.getStatus().equals("200")) {
//            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
//        }
//        Challenge challenge = challengeResponse.getData();
//        ApiResponse<Void> fileResponse = validateFile(answerFile);
//        if (!fileResponse.getStatus().equals("200")) {
//            return new ApiResponse<>(fileResponse.getStatus(), fileResponse.getMessage(), null);
//        }
//
//        Member member = memberService.findMemberByLoginId();
//        Family family = member.getFamily();
//        if (family == null) {
//            return new ApiResponse<>("404", "Family not found for the current member", null);
//        }
//        Lucky lucky = luckyService.findCurrentLucky(family.getFamilyId());
//
//        if (answerFile.getContentType().startsWith("image/")) {
//            // 이미지 업로드 동기 처리
//            try {
//                Map<String, String> response = sharedFileService.uploadImageFile(challenge, answerFile, FileType.IMAGE);
//                // 병합 작업을 비동기적으로 예약
//                sharedFileService.scheduleMergeImages(challenge.getChallengeNum(), lucky);
//                return new ApiResponse<>("200", response.get("message"), response);
//            } catch (Exception e) {
//                log.error("Image upload failed", e);
//                return new ApiResponse<>("500", "Internal Server Error", null);
//            }
//
//        } else if (answerFile.getContentType().startsWith("video/")) {
//            // 비디오 업로드 동기 처리
//            try {
//                String uploadedUrl = awsS3Service.uploadOriginalFile(answerFile);
//                answerService.modifyAnswer(challengeId, uploadedUrl);
//                // 병합 작업을 비동기적으로 예약
//                sharedFileService.scheduleMergeImages(challenge.getChallengeNum(), lucky);
//                return new ApiResponse<>("200", "Video uploaded successfully", Map.of("url", uploadedUrl));
//            } catch (Exception e) {
//                log.error("Video upload failed", e);
//                return new ApiResponse<>("500", "Internal Server Error", null);
//            }
//        } else {
//            return new ApiResponse<>("400", "Invalid file type: " + answerFile.getContentType(), null);
//        }
//    }
// 화상 통화 챌린지 결과 저장
@PostMapping("/face")
public ApiResponse<Map<String, String>> saveFaceTimeAnswer(
        @RequestParam("challengeId") Long challengeId,
        @RequestPart("answer") MultipartFile answerFile) throws Exception {

    ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.FACETIME);
    if (!challengeResponse.getStatus().equals("200")) {
        return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
    }
    Challenge challenge = challengeResponse.getData();
    ApiResponse<Void> fileResponse = validateFile(answerFile);
    if (!fileResponse.getStatus().equals("200")) {
        return new ApiResponse<>(fileResponse.getStatus(), fileResponse.getMessage(), null);
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
        String uploadedUrl = awsS3Service.uploadOriginalFile(answerFile);

        // Answer 업데이트
        answerService.modifyAnswer(challengeId, uploadedUrl);

//            // TODO: 원래 하던 코드 (4번째 cut 사진이 병합이 안되는 현상 발생)
//            // 그룹별 4개의 이미지가 모였는지 확인 및 병합
//            sharedFileService.checkAndMergeImages(challenge.getChallengeNum(), lucky);

        // TODO: 방법 1: 이미지 업로드와 병합 분리 (4번째 cut 1장 만들어짐) -> 채택!
        // 이미지 업로드가 완료된 후에 병합을 시도합니다.
        sharedFileService.mergeImagesIfNeeded(challenge.getChallengeNum(), lucky);

//            // TODO: 방법 2: 병합을 서버 측에서 스케줄링 (첫번째 cut만 생성됨)
//            // 이미지 업로드가 완료된 후에 병합을 예약합니다.
//            sharedFileService.scheduleMergeImages(challenge.getChallengeNum(), lucky);

//            // TODO: 비동기 시도...
//            // 비디오 업로드 후 이미지 병합 작업 스케줄링
//            sharedFileService.scheduleMergeImages(challenge.getChallengeNum(), lucky);

        return new ApiResponse<>("200", "Video uploaded successfully", Map.of("url", uploadedUrl));
    } else {
        return new ApiResponse<>("400", "Invalid file type: " + answerFile.getContentType(), null);
    }
}

    // 화상 통화 챌린지 결과 조회
    @GetMapping("/face/result")
    public ApiResponse<Map<Integer, List<String>>> getFaceTimeAnswer(
            @RequestParam("challengeId") Long challengeId) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.FACETIME);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        Member member = memberService.findMemberByLoginId();
        Family family = member.getFamily();
        if (family == null) {
            return new ApiResponse<>("404", "Family not found for the current member", null);
        }

        // 챌린지 조회 시 조회수 증가
        Lucky matchedLucky = luckyService.findMatchingLucky(challengeId, member);
        if (matchedLucky != null && matchedLucky.isRunning()) {
            luckyService.increaseChallengeViews(matchedLucky.getLuckyId(), challenge.getChallengeNum());
        }
        assert matchedLucky != null;

        Map<Integer, List<String>> results = sharedFileService.getFaceChallengeResults(challenge.getChallengeNum(), matchedLucky.getLuckyId());

        // 병합 이미지가 하나도 없는지 확인
        boolean areMergedImagesPresent = results.values().stream().anyMatch(list -> !list.isEmpty());

        if (!areMergedImagesPresent) {
            // 병합된 이미지가 하나도 없다면, 다시 병합 작업을 예약
            sharedFileService.scheduleMergeImages(challenge.getChallengeNum(), matchedLucky);
        }

        return new ApiResponse<>("200", "FaceTime Challenge results found successfully", results);
    }

    // 음성 챌린지 조회
    @GetMapping("/voice")
    public ApiResponse<VoiceChallengeDto> getVoiceChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.VOICE1, ChallengeType.VOICE2, ChallengeType.VOICE3, ChallengeType.VOICE4);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        Long challengeDate = answerService.findDateByChallengeMember(challenge, member);
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        List<Answer> allAnswers = answerService.findAnswersByChallenges(challenge, member);     // 특정 음성 챌린지에 매핑된 answer list 찾기

        // 챌린지 조회 시 조회수 증가
        Lucky matchedLucky = luckyService.findMatchingLucky(challengeId, member);
        if (matchedLucky != null && matchedLucky.isRunning()){
            luckyService.increaseChallengeViews(matchedLucky.getLuckyId(), challenge.getChallengeNum());
        }

        VoiceChallengeDto voiceChallengeDto = new VoiceChallengeDto(challenge, isComplete, challengeDate, allAnswers);
        return new ApiResponse<>("200", "Voice Challenge found successfully", voiceChallengeDto);
    }

    // 음성 챌린지 수정 (바뀔 예정 있으므로 중복 처리 아직 안함)
    @PostMapping("/voice")
    public ApiResponse<Map<String, String>> saveVoiceAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestPart("answer") MultipartFile answerFile) throws Exception {
        ApiResponse<Challenge> challengeResponse = validateChallenge(challengeId, ChallengeType.VOICE1, ChallengeType.VOICE2, ChallengeType.VOICE3, ChallengeType.VOICE4);
        if (!challengeResponse.getStatus().equals("200")) {
            return new ApiResponse<>(challengeResponse.getStatus(), challengeResponse.getMessage(), null);
        }
        Challenge challenge = challengeResponse.getData();
        ApiResponse<Void> fileResponse = validateFile(answerFile);
        if (!fileResponse.getStatus().equals("200")) {
            return new ApiResponse<>(fileResponse.getStatus(), fileResponse.getMessage(), null);
        }

        // S3에 파일 업로드
        String uploadVoiceUrl = awsS3Service.uploadFile(answerFile);

        // Answer 업데이트
        Answer answer = answerService.modifyAnswer(challengeId, uploadVoiceUrl);

        // STT 처리
        TranscriptionRequest transcriptionRequest = new TranscriptionRequest();
        transcriptionRequest.setFile(answerFile);
        WhisperTranscriptionResponse transcriptionResponse = openAIClientService.createTranscription(transcriptionRequest);


        // Map 형태로 답변 URL 반환
        Map<String, String> responseData = new HashMap<>();
        if (transcriptionResponse.getText() == null) {
            return new ApiResponse<>("500", "STT conversion failed, no text returned", null);
        }
        responseData.put("answer", transcriptionResponse.getText());


        // 단어 리스트에서 특정 단어 검색 및 자르기
        List<WhisperTranscriptionResponse.word> words = transcriptionResponse.getWords();
        String targetWord = getTargetWordForChallengeType(challenge.getChallengeType());
        WhisperTranscriptionResponse.word target = findTargetWord(words, targetWord);

        if (target == null) {
            return new ApiResponse<>("200", "Voice STT Success But Not Find Target Word", responseData);
        }

        // 비동기 작업 시작(음성 자르는것은 비동기로 실행)
        Member member = answer.getMember();
        Lucky lucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        sharedFileService.processAndUploadCutAudio(answerFile, targetWord, challengeId, lucky, member, target, challenge);
        return new ApiResponse<>("200", "Voice STT Success", responseData);

    }

    // 챌린지 검증 헬퍼 메서드
    private ApiResponse<Challenge> validateChallenge(Long challengeId, ChallengeType... validTypes) {
        // 챌린지 조회
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge with provided challengeId not found", null);
        }
        // 챌린지 유형에 맞게 접근하도록 추가 : 챌린지ID로 가져온 챌린지가 Mapping된 챌린지 유형에 맞지 않으면 오류 반환
        if (!Arrays.asList(validTypes).contains(challenge.getChallengeType())) {
            return new ApiResponse<>("400", "Invalid challenge type for the provided challengeId", null);
        }
        return new ApiResponse<>("200", "Challenge validated", challenge);
    }

    // 파일 검증 헬퍼 메서드
    private ApiResponse<Void> validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ApiResponse<>("400", "Answer file is missing", null);
        }
        return new ApiResponse<>("200", "File validated", null);
    }

    // 멤버에 맞는 타겟 단어 찾기
    private String getTargetWordForChallengeType(ChallengeType challengeType) {
        switch (challengeType) {
            case VOICE1:
                return "우리";
            case VOICE2:
                return "행복하자";
            case VOICE3:
                return "많이";
            case VOICE4:
                return "사랑해";
            default:
                return "";
        }
    }
    // whisper가 준 단어에서 타겟 단어 찾기
    private WhisperTranscriptionResponse.word findTargetWord(List<WhisperTranscriptionResponse.word> words, String targetWord) {
        for (WhisperTranscriptionResponse.word word : words) {
            log.info(String.valueOf(word));
            if (word.getWord().equals(targetWord)) {
                return word;
            }
        }
        return null;
    }

}
