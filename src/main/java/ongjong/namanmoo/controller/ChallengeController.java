package ongjong.namanmoo.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.dto.challenge.*;
import ongjong.namanmoo.service.DateUtil;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.domain.answer.Answer;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.answer.ModifyAnswerDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Collections;
import java.util.List;
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
    public ApiResponse saveChallenge(@RequestBody SaveChallengeRequest request) throws Exception {
        Long challengeDate = request.getChallengeDate();
        Long familyId = familyService.findFamilyId();
        if (!luckyService.createLucky(familyId, challengeDate) || !answerService.createAnswer(familyId, challengeDate)) {
            return new ApiResponse("404", "Challenge not found", null);
        }
        return new ApiResponse("200", "Success", null);
    }

    @GetMapping("/today")     // 오늘의 챌린지 조회
    public ApiResponse getChallenge(@RequestParam("challengeDate") Long challengeDate) throws Exception {
        List<Challenge> challenges = challengeService.findChallengeByMemberId(challengeDate);
        Challenge challenge = challengeService.findCurrentChallenge(challenges);
        if (challenge == null) {
            return new ApiResponse("404", "Challenge not found", null);
        }
        Integer currentNum  = challengeService.findCurrentNum(challengeDate);
        DateUtil dateUtil = DateUtil.getInstance();
        ChallengeDto challengeDto = new ChallengeDto(challenge, currentNum, dateUtil.timestampToString(challengeDate));
        return new ApiResponse("200", "Success", challengeDto);
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
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        Long challengeDate = answerService.findDateByChallengeMember(challenge);
        List<Answer> answers = answerService.findAnswerByChallengeandFamily(challenge, member);

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        NormalChallengeDto normalChallengeDto = new NormalChallengeDto(challenge, isComplete, challengeDate, answers);
        return new ApiResponse<>("success", "Challenge retrieved successfully", normalChallengeDto);      // 객체를 리스트 형태로 감싸서 반환
    }

    // 일반 챌린지 내용 수정 -> 새 내용, 수정날짜 저장
    @PostMapping("/normal")
    public  ApiResponse saveNormalAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        Long challengeId = request.getChallengeId();
        String answerContent = request.getAnswerContent();
        Answer answer = answerService.modifyAnswer(challengeId, answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse("200", "Success", modifyAnswerDto);
    }

    // 그룹 챌린지 조회
    @GetMapping("/group")
    public ApiResponse<GroupChallengeDto> getGroupChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {
        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null);
        }

        Long challengeDate = answerService.findDateByChallengeMember(challenge);
        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        List<Answer> allAnswers = answerService.findAnswersByChallenges(challenge, member);     // 특정 그룹 챌린지에 매핑된 answer list 찾기

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        GroupChallengeDto groupChallengeDto = new GroupChallengeDto(challenge, challengeDate, isComplete, allAnswers);
        return new ApiResponse<>("success", "Challenge retrieved successfully", groupChallengeDto);
    }

    // 그룹 챌린지 답변 수정
    @PostMapping("/group")
    public  ApiResponse saveGroupAnswer(@RequestBody SaveAnswerRequest request) throws Exception {
        Long challengeId = request.getChallengeId();
        String answerContent = request.getAnswerContent();
        log.info("aaaaa: {}",answerContent);
        Answer answer = answerService.modifyAnswer(challengeId, answerContent);
        ModifyAnswerDto modifyAnswerDto = new ModifyAnswerDto(answer);
        return new ApiResponse("200", "Success", modifyAnswerDto);
    }

    // 사진 챌린지 조회
    @GetMapping("/photo")
    public ApiResponse<PhotoChallengeDto> getPhotoChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기

        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        Long challengeDate = answerService.findDateByChallengeMember(challenge);
        List<Answer> answers = answerService.findAnswerByChallengeandFamily(challenge, member);

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        PhotoChallengeDto photoChallengeDto = new PhotoChallengeDto(challenge, isComplete, challengeDate, answers);
        return new ApiResponse<>("success", "Challenge retrieved successfully",photoChallengeDto);      // 객체를 리스트 형태로 감싸서 반환
    }

    // 사진 챌린지 수정
    @PostMapping("/photo")
    public ApiResponse<Map<String, String>> savePhotoAnswer(
            @RequestPart("challengeId") Long challengeId,
            @RequestPart("answer") MultipartFile answerFile) throws Exception {

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

        // 멤버와 챌린지를 통해 timestamp 조회
        Long timestamp = answerService.findDateByChallengeMember(challenge);

        // 가족 초대 코드 조회 (멤버를 통해 가족 정보를 가져온 후 초대 코드 획득)
        Member member = memberService.findMemberByLoginId();
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
    public ApiResponse<String> saveFaceTimeAnswer(
            @RequestParam("challengeId") Long challengeId,
            @RequestParam("familyPhotos") MultipartFile[] familyPhotos) throws Exception {

        if (familyPhotos.length != 4) {
            return new ApiResponse<>("400", "Exactly 4 photos must be uploaded", null);
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
        Answer answer = answerService.modifyAnswer(challengeId, jsonAnswerContent);

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
        Family family = member.getFamily();

        if (family == null) {
            return new ApiResponse<>("404", "Family not found for the current member", null);
        }

        // 가족의 모든 멤버를 순회하며 답변을 찾음
        List<Member> members = family.getMembers();
        Answer foundAnswer = null;

        for (Member familyMember : members) {
            Optional<Answer> answerOpt = answerService.findAnswerByChallengeAndMember(challenge, familyMember);
            if (answerOpt.isPresent() && answerOpt.get().getAnswerContent() != null) {
                foundAnswer = answerOpt.get();
                break;
            }
        }

        if (foundAnswer == null) {
            return new ApiResponse<>("404", "Answer not found for the provided challengeId in any family member", null);
        }

        String answerContent = foundAnswer.getAnswerContent();

        // JSON 형식으로 변환된 URL 목록을 Map 형태로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> familyPhotos = objectMapper.readValue(answerContent, new TypeReference<List<String>>() {});

        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("challengeTitle", challenge.getChallengeTitle());
        responseData.put("familyPhotos", familyPhotos);

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        return new ApiResponse<>("200", "Success", responseData);
    }

    // 음성 챌린지 조회
    @GetMapping("/voice")
    public ApiResponse<VoiceChallengeDto> getVoiceChallenge(@RequestParam("challengeId") Long challengeId) throws Exception {

        Challenge challenge = challengeService.findChallengeById(challengeId);
        if (challenge == null) {
            return new ApiResponse<>("failure", "Challenge not found for the provided challengeId", null);
        }

        Member member = memberService.findMemberByLoginId(); // 로그인한 멤버 찾기
        boolean isComplete = answerService.findIsCompleteAnswer(challenge, member);
        Long challengeDate = answerService.findDateByChallengeMember(challenge);
        List<Answer> answers = answerService.findAnswerByChallengeandFamily(challenge, member);

        // 챌린지 조회 시 조회수 증가
        Lucky currentLucky = luckyService.findCurrentLucky(member.getFamily().getFamilyId());
        luckyService.increaseChallengeViews(currentLucky.getLuckyId(), challenge.getChallengeNum());

        VoiceChallengeDto voiceChallengeDto = new VoiceChallengeDto(challenge, isComplete, challengeDate, answers);
        return new ApiResponse<>("success", "Challenge retrieved successfully",voiceChallengeDto);      // 객체를 리스트 형태로 감싸서 반환
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
