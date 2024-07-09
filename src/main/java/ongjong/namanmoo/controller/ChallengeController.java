package ongjong.namanmoo.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.dto.challenge.*;
import ongjong.namanmoo.global.security.util.DateUtil;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.answer.ModifyAnswerDto;
import ongjong.namanmoo.repository.AnswerRepository;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.status;

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

    @PostMapping     // 챌린지 생성 -> 캐릭터 생성 및 답변 생성
    public ResponseEntity<ApiResponse> saveChallenge(@RequestBody SaveChallengeRequest request) throws Exception {
        Long challengeDate = request.getChallengeDate();
        Long familyId = familyService.findFamilyId();
        if (!luckyService.join(familyId) || !answerService.createAnswer(familyId, challengeDate)) {
            return status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("404", "Challenge not found", null));
        }
        return status(HttpStatus.OK)
                .body(new ApiResponse("200", "Success", null));
    }

    @GetMapping("/today")     // 오늘의 챌린지 조회
    public ResponseEntity<ApiResponse> getChallenge(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        List<Challenge> challenges = challengeService.findChallengeByMemberId(challengeDate);
        Challenge challenge = challengeService.findCurrentChallenge(challenges);
        if (challenge == null) {
            return status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("404", "Challenge not found", null));
        }
        Long currentNum  = challengeService.findCurrentNum(challengeDate);
        DateUtil dateUtil = DateUtil.getInstance();
        ChallengeDto challengeDto = new ChallengeDto(challenge, currentNum, dateUtil.timestampToString(challengeDate));
        return status(HttpStatus.OK)
                .body(new ApiResponse("200", "Success", challengeDto));
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
    public ApiResponse<List<NormalChallengeDto>> getNormalChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        Long challengeDate = answerService.findDateByChallengeMember(challenge);
        List<Answer> answers = answerService.findAnswerByChallenge(challenge);

        NormalChallengeDto normalChallengeDto = new NormalChallengeDto(challenge, isComplete, challengeDate, answers);

        return new ApiResponse<>("success", "Challenge retrieved successfully", Collections.singletonList(normalChallengeDto));      // 객체를 리스트 형태로 감싸서 반환
    }

    @PostMapping("/normal")     // 일반 챌린지 내용 수정 -> 새 내용, 수정날짜 저장
    public  ResponseEntity<ApiResponse> saveAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        Long challengeId = request.getChallengeId();
        String answerContent = request.getAnswerContent();
        Answer answer = answerService.modifyAnswer(challengeId, answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return status(HttpStatus.OK)
                .body(new ApiResponse("200", "Success", modifyAnswerDto));
    }

//    // 그룹 챌린지 조회
//    @GetMapping("/group")      // 일반 챌린지 조회
//    public ApiResponse<List<GroupChallengeDto>> getGroupChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
//        Challenge challenge = challengeService.findChallengeById(challengeId);
//
//    }







    // 사진 챌린지 조회
    @GetMapping("/photo")
    public ApiResponse<List<PhotoChallengeDto>> getPhotoChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기

        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        Long challengeDate = answerService.findDateByChallengeMember(challenge);
        List<Answer> answers = answerService.findAnswerByChallenge(challenge);

        PhotoChallengeDto photoChallengeDto = new PhotoChallengeDto(challenge, isComplete, challengeDate, answers);

        return new ApiResponse<>("success", "Challenge retrieved successfully", Collections.singletonList(photoChallengeDto));      // 객체를 리스트 형태로 감싸서 반환
    }

    // 사진 챌린지 수정
    @PostMapping("/photo")
    public ApiResponse<Map<String, String>> savePhotoAnswer(
            @ModelAttribute PhotoAnswerRequest photoAnswerRequest) throws Exception {
//            @RequestPart("challengeId") Long challengeId,
//            @RequestPart("answer") MultipartFile answerFile) throws Exception {

        Long challengeId = photoAnswerRequest.getChallengeId();
        MultipartFile answerFile = photoAnswerRequest.getAnswer();

        Member member = memberService.findMemberByLoginId();
        Challenge challenge = challengeService.findChallengeById(challengeId);

        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found", null);
        }

        if (answerFile == null || answerFile.isEmpty()) {
            return new ApiResponse<>("400", "Answer file is missing", null);
        }

        // S3에 파일 업로드
        String uploadImageUrl = awsS3Service.uploadFile(answerFile);

        // Answer 객체 수정 및 저장
        Optional<Answer> optionalAnswer = answerService.findAnswerByChallengeAndMember(challenge, member);
        if (optionalAnswer.isPresent()) {
            Answer answer = optionalAnswer.get();
            answer.setAnswerContent(uploadImageUrl);
            answer.setModifiedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
            answerService.saveAnswer(answer);

            // Map 형태로 답변 URL 반환
            Map<String, String> responseData = new HashMap<>();
            responseData.put("answer", uploadImageUrl);

            return new ApiResponse<>("200", "Success", responseData);
        } else {
            return new ApiResponse<>("404", "Answer not found", null);
        }
    }

    // 화상 통화 챌린지 조회
    @GetMapping("/face")
    public ApiResponse<ChallengeDto> getFaceChallenge(
            @RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }

        // 챌린지 번호를 가져옴 (예를 들어, 해당 챌린지의 현재 진행 번호)
        Long currentNum = challengeService.findCurrentNum(challengeId);
        // 챌린지와 멤버를 통해 timestamp를 가져옴
        Long timestamp = answerService.findDateByChallengeMember(challenge);

        // 화상 통화 챌린지 정보 가져오기
        ChallengeDto challengeDto = new ChallengeDto(challenge, currentNum, timestamp.toString());

        return new ApiResponse<>("200", "Success", challengeDto);
    }

    // 화상 통화 챌린지 결과 저장
    @PostMapping("/face")
    public ApiResponse<String> saveFaceTimeAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestParam("familyPhotos") MultipartFile[] familyPhotos) throws Exception {

        if (familyPhotos.length != 4) {
            return new ApiResponse<>("400", "Exactly 4 photos must be uploaded", null);
        }

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId();
        Optional<Answer> existingAnswer = answerService.findAnswerByChallengeAndMember(challenge, member);

        if (existingAnswer.isEmpty()) {
            return new ApiResponse<>("404", "Answer not found for the provided challengeId and member", null);
        }

        // S3에 파일 업로드 및 URL 저장
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile photo : familyPhotos) {
            String uploadedUrl = awsS3Service.uploadFile(photo);
            uploadedUrls.add(uploadedUrl);
        }

        // JSON 형식으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonAnswerContent = objectMapper.writeValueAsString(uploadedUrls);

        // Answer 업데이트
        Answer answer = existingAnswer.get();
        answer.setAnswerContent(jsonAnswerContent);
        answer.setModifiedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
        answerService.saveAnswer(answer);

        return new ApiResponse<>("200", "Success", null);
    }

    // 화상 통화 챌린지 결과 조회
    @GetMapping("/face/result")
    public ApiResponse<Map<String, Object>> getFaceTimeAnswer(
            @RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("404", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId();
        Optional<Answer> existingAnswer = answerService.findAnswerByChallengeAndMember(challenge, member);

        if (existingAnswer.isEmpty()) {
            return new ApiResponse<>("404", "Answer not found for the provided challengeId and member", null);
        }

        Answer answer = existingAnswer.get();
        String answerContent = answer.getAnswerContent();

        // JSON 형식으로 변환된 URL 목록을 Map 형태로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> familyPhotos = objectMapper.readValue(answerContent, new TypeReference<List<String>>() {});

        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("challengeTitle", challenge.getChallengeTitle());
        responseData.put("familyPhotos", familyPhotos);

        return new ApiResponse<>("200", "Success", responseData);
    }


    @GetMapping("/voice")      // 음성 챌린지 조회
    public ApiResponse<List<VoiceChallengeDto>> getVoiceChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        Long challengeDate = answerService.findDateByChallengeMember(challenge);
        List<Answer> answers = answerService.findAnswerByChallenge(challenge);

        VoiceChallengeDto voiceChallengeDto = new VoiceChallengeDto(challenge, isComplete, challengeDate, answers);

        return new ApiResponse<>("success", "Challenge retrieved successfully", Collections.singletonList(voiceChallengeDto));      // 객체를 리스트 형태로 감싸서 반환
    }

    @Data
    static class SaveChallengeRequest {
        private Long challengeDate;
    }

    @Data
    static class SaveAnswerRequest{
        private Long challengeId;
        private String answerContent;
    }

    @Data
    public static class PhotoAnswerRequest {
        private Long challengeId;
        private MultipartFile answer;
    }

}
