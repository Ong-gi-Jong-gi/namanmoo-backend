package ongjong.namanmoo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.FileType;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.SharedFile;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.recap.MemberAndCountDto;
import ongjong.namanmoo.dto.recap.MemberPhotosAnswerDto;
import ongjong.namanmoo.dto.recap.MemberRankingListDto;
import ongjong.namanmoo.dto.recap.MemberYouthAnswerDto;
import ongjong.namanmoo.dto.recap.*;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.global.security.util.CustomMultipartFile;
import ongjong.namanmoo.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/recap")
@RequiredArgsConstructor
public class RecapController {

    private final LuckyService luckyService;
    private final AnswerService answerService;
    private final ChallengeService challengeService;
    private final MemberService memberService;
    private final AwsS3Service awsS3Service;
    private final FFmpegService ffmpegService;
    private final SharedFileService sharedFileService;

    // 행운이 리스트
    @GetMapping("/list")
    public ResponseEntity<?> getLuckyList() {
        List<LuckyListDto> luckyListStatus = luckyService.getLuckyListStatus();
        return ResponseEntity.ok().body(new ApiResponse<>("200", "Success", luckyListStatus));
    }

    // recap 랭킹
    // facetime 테이블 지우면 정상적으로 작동
    @GetMapping("/ranking")
    public ApiResponse<MemberRankingListDto> getRanking(@RequestParam("luckyId") Long luckyId){
        Lucky lucky = luckyService.getLucky(luckyId);
        Integer totalCount = lucky.getStatus();
        Integer luckyStatus = luckyService.calculateLuckyStatus(lucky);
        List<MemberAndCountDto> memberAndCountList = memberService.getMemberAndCount(lucky);
        MemberRankingListDto responseDto = new MemberRankingListDto(totalCount,luckyStatus,memberAndCountList);
        return new ApiResponse<>("200", "Ranking retrieved successfully", responseDto);
    }

    // recap 화상통화
    @GetMapping("/face")
    public ApiResponse<MemberFacetimeDto> getFacetime(@RequestParam("luckyId") Long luckyId) throws Exception {
        MemberFacetimeDto answerList = answerService.getFacetimeAnswerList(luckyId);
        return new ApiResponse<>("200", "facetime retrieved successfully", answerList);
    }

    // 리캡 컨텐츠 조회 - 통계
    @GetMapping("/statistics")
    public ApiResponse<List<Map<String, Object>>> getStatistics(@RequestParam("luckyId") Long luckyId) throws Exception {
        Lucky lucky = luckyService.getLucky(luckyId);

        // 가장 조회수가 많은 챌린지
        Challenge mostViewedChallenge = challengeService.findMostViewedChallenge(lucky);
        Map<String, Object> mostViewedData = new HashMap<>();
        mostViewedData.put("topic", "mostViewed");
        mostViewedData.put("topicResult", mostViewedChallenge != null ? lucky.getChallengeViews().get(mostViewedChallenge.getChallengeNum()) : 0);
        mostViewedData.put("challengeId", mostViewedChallenge != null ? mostViewedChallenge.getChallengeId() : "");
        mostViewedData.put("challengeType", mostViewedChallenge != null ? mostViewedChallenge.getChallengeType() : "");
        mostViewedData.put("challengeNumber", mostViewedChallenge != null ? mostViewedChallenge.getChallengeNum() : "");
        mostViewedData.put("challengeTitle", mostViewedChallenge != null ? mostViewedChallenge.getChallengeTitle() : "");

        // 모두가 가장 빨리 답한 챌린지
        Challenge fastestAnsweredChallenge = challengeService.findFastestAnsweredChallenge(lucky);
        Map<String, Object> fastestAnsweredData = new HashMap<>();
        fastestAnsweredData.put("topic", "fastestAnswered");
        fastestAnsweredData.put("topicResult", fastestAnsweredChallenge != null ? challengeService.calculateLatestResponseTime(lucky, fastestAnsweredChallenge) : 0);
        fastestAnsweredData.put("challengeId", fastestAnsweredChallenge != null ? fastestAnsweredChallenge.getChallengeId() : "");
        fastestAnsweredData.put("challengeType", fastestAnsweredChallenge != null ? fastestAnsweredChallenge.getChallengeType() : "");
        fastestAnsweredData.put("challengeNumber", fastestAnsweredChallenge != null ? fastestAnsweredChallenge.getChallengeNum() : "");
        fastestAnsweredData.put("challengeTitle", fastestAnsweredChallenge != null ? fastestAnsweredChallenge.getChallengeTitle() : "");

        return new ApiResponse<>("200", "statistics retrieved successfully", Arrays.asList(mostViewedData, fastestAnsweredData));
    }

    // recap 과거 사진
    // challengeNum => 13: 나의 어렸을 때 장래희망
    // cahllengeNum => 28 : 자신의 어렸을 적 사진 ( 23 : 학생 때 졸업사진 , 9: 가장 마음에 드는 본인 사진)

    @GetMapping("/youth")
    public ApiResponse<List<MemberYouthAnswerDto>> getYouth(@RequestParam("luckyId") Long luckyId) throws Exception{
        List<MemberYouthAnswerDto> memberAnswerDtoList = answerService.getYouthByMember(luckyId, 14, 1);
        return new ApiResponse<>("200", "Youth photos retrieved successfully", memberAnswerDtoList);
    }

    // recap 미안한점 고마운점
    @GetMapping("/appreciations")
    public ApiResponse<List<MemberAppreciationDto>> getAppreciations(@RequestParam("luckyId") Long luckyId) throws Exception {
        List<MemberAppreciationDto> appreciationList = answerService.getAppreciationByMember(luckyId, 27, 29);
        return new ApiResponse<>("200", "Success", appreciationList);
    }


    // recap 가족사진
    @GetMapping("/photos")
    public ApiResponse<MemberPhotosAnswerDto> getPhotos(@RequestParam("luckyId") Long luckyId) throws Exception {
        MemberPhotosAnswerDto photosAnswerDto = answerService.getPhotos(luckyId);
        return new ApiResponse<>("200", "Success", photosAnswerDto);
    }

    // recap 음성
    @GetMapping("/voice")
    public ApiResponse<Map<String, String>> mergeVoiceClips(@RequestParam("luckyId") Long luckyId) {
        List<File> localFiles = null;
        File outputFile = null;

        try {
            Lucky lucky = luckyService.getLucky(luckyId);

            // 이미 병합된 음성 파일이 있는지 확인
            SharedFile mergeVoice = sharedFileService.getMergeVoice(lucky, FileType.AUDIO);
            if (mergeVoice != null) {
                Map<String, String> data = new HashMap<>();
                data.put("backgroundVoice", mergeVoice.getFileName());
                return new ApiResponse<>("200", "Already Voice Clips Merge Success", data);
            }

            // S3에서 해당 luckyId의 음성파일 목록을 가져옴
            List<String> audioPaths = awsS3Service.listAudioFiles(luckyId);
            if (audioPaths.size() != 4) {
                return new ApiResponse<>("404", "Not Enough Voice Clips", null);
            }

            // VOICE_ 순서대로 정렬
            audioPaths.sort((path1, path2) -> {
                try {
                    int num1 = Integer.parseInt(path1.split("VOICE_")[1].split("\\.")[0]);
                    int num2 = Integer.parseInt(path2.split("VOICE_")[1].split("\\.")[0]);
                    return Integer.compare(num1, num2);
                } catch (NumberFormatException e) {
                    log.error("Error parsing VOICE_ number from path: {} or {}", path1, path2, e);
                    return 0;
                }
            });

            // 임시 디렉토리 생성
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Files.createDirectories(tempDir);  // Ensure the directory exists

            // 오디오 파일들을 임시 디렉토리로 다운로드
            localFiles = audioPaths.stream().map(path -> {
                File localFile = new File(tempDir.toString(), Paths.get(path).getFileName().toString());
                awsS3Service.downloadFile(path, localFile);
                return localFile;
            }).collect(Collectors.toList());

            // 출력 파일명 설정
            String outputFileName = "merged_output_" + luckyId + ".mp3";
            String outputPath = tempDir.resolve(outputFileName).toString();

            // 오디오 파일 병합(사이에 0.2초 침묵 삽입)
            ffmpegService.mergeAudiosWithSilence(localFiles.stream().map(File::getPath).collect(Collectors.toList()), outputPath, 0.1);

            // 병합된 파일 S3업로드
            outputFile = new File(outputPath);
            String s3Url = awsS3Service.uploadFile(new CustomMultipartFile(outputFile));

            // 디비 저장
            sharedFileService.uploadMergeVoice(lucky, s3Url, FileType.AUDIO);

            // 성공 응답 생성
            Map<String, String> data = new HashMap<>();
            data.put("backgroundVoice", s3Url);
            return new ApiResponse<>("200", "Voice Clips Merge Success", data);
        } catch (Exception e) {
            log.error("Error merging voice clips", e);
            return new ApiResponse<>("500", "Voice Merge Fail: " + e.getMessage(), null);
        } finally {
            // 임시 파일 삭제
            if (localFiles != null) {
                localFiles.forEach(file -> {
                    if (file.exists() && !file.delete()) {
                        log.warn("Failed to delete temporary file: {}", file.getPath());
                    }
                });
            }
            if (outputFile != null && outputFile.exists() && !outputFile.delete()) {
                log.warn("Failed to delete temporary output file: {}", outputFile.getPath());
            }
        }
    }

    // 중복 해결 예정??
//    private Map<String, Object> buildChallengeData(String topic, Challenge challenge, Lucky lucky, Long topicResult) {
//        Map<String, Object> data = new HashMap<>();
//        data.put("topic", topic);
//        data.put("topicResult", topicResult);
//        data.put("challengeId", challenge != null ? challenge.getChallengeId() : "");
//        data.put("challengeType", challenge != null ? challenge.getChallengeType() : "");
//        data.put("challengeNumber", challenge != null ? challenge.getChallengeNum() : "");
//        data.put("challengeTitle", challenge != null ? challenge.getChallengeTitle() : "");
//        return data;
//    }
}
