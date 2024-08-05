package ongjong.namanmoo.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.*;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.dto.openAI.WhisperTranscriptionResponse;
import ongjong.namanmoo.global.security.util.CustomMultipartFile;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.SharedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@EnableAsync
@Service
public class SharedFileService {

    @Autowired
    private MemberService memberService;
    @Autowired
    private AwsS3Service awsS3Service;
    @Autowired
    private LuckyRepository luckyRepository;
    @Autowired
    private SharedFileRepository sharedFileRepository;
    @Autowired
    private FFmpegService ffmpegService;

    private final AmazonS3 amazonS3Client;
    private final String bucket;
    private final String region;

    private final ReentrantLock lock = new ReentrantLock();
    // 병합 작업 상태를 저장하는 Map
    private final Map<String, Boolean> mergeTasks = new ConcurrentHashMap<>();

    public SharedFileService(
            @Value("${cloud.aws.credentials.access-key}") String accessKeyId,
            @Value("${cloud.aws.credentials.secret-key}") String secretKey,
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${cloud.aws.region.static}") String region) {

        // AWS 인증 정보 생성
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretKey);

        // AmazonS3 클라이언트 생성
        this.amazonS3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        // 필드 초기화
        this.bucket = bucket;
        this.region = region;
    }

    @Transactional
    public Map<String, String> uploadImageFile(Challenge challenge, MultipartFile photo, FileType fileType) throws Exception {
        Map<String, String> response = new HashMap<>();

        Member member = memberService.findMemberByLoginId();
        Family family = member.getFamily();

        if (fileType != FileType.IMAGE) {
            throw new IllegalArgumentException("Invalid file type: " + fileType);
        }

        try {
            // S3에 파일 업로드 및 URL 저장
//            String uploadedUrl = awsS3Service.uploadFile(photo);
            String uploadedUrl = awsS3Service.uploadOriginalFile(photo, member.getMemberId());
//            String uploadedUrl = awsS3Service.uploadFileWithRetry(photo, 3);

            Optional<Lucky> optionalLucky = luckyRepository.findByFamilyFamilyIdAndRunningTrue(family.getFamilyId());
            Lucky lucky = optionalLucky.orElseThrow(() -> new RuntimeException("Running Lucky not found for family"));

            // SharedFile 엔티티 저장
            SharedFile sharedFile = new SharedFile();
            sharedFile.setFileName(uploadedUrl);
            sharedFile.setFileType(FileType.IMAGE);
            sharedFile.setChallengeNum(challenge.getChallengeNum());
            sharedFile.setCreateDate(System.currentTimeMillis());
            sharedFile.setLucky(lucky);
            sharedFileRepository.save(sharedFile);
            log.info("SharedFile saved successfully: {}", sharedFile.getSharedFileId());

            response.put("url", uploadedUrl);
            response.put("message", "Photo uploaded successfully");

        } catch (Exception e) {
            log.error("Error occurred while uploading image file", e);
            throw e;
        }

        return response;
    }

    // 병합된 이미지를 S3에 업로드하는 메서드
    public String uploadMergedImageToS3(BufferedImage mergedImage, String bucketName, String fileObjKeyName) throws IOException {

        // 최적화된 이미지로 변환
//        BufferedImage optimizedImage = optimizeImage(mergedImage);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        ImageIO.write(optimizedImage, "png", os);
        ImageIO.write(mergedImage, "png", os);
        byte[] buffer = os.toByteArray();
        InputStream is = new ByteArrayInputStream(buffer);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(buffer.length);
        meta.setContentType("image/png");
        try {
            log.info("파일 업로드 시작: {}", fileObjKeyName);
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileObjKeyName, is, meta).withCannedAcl(CannedAccessControlList.PublicRead));
            log.info("파일 업로드 완료: {}", fileObjKeyName);

            // 선택: amazonS3Client.getUrl(bucketName, fileObjKeyName).toString(); 또는 String.format을 사용
            return amazonS3Client.getUrl(bucketName, fileObjKeyName).toString();
            // 또는
            // return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileObjKeyName);
        } catch (AmazonServiceException e) {
            log.error("Amazon 서비스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (SdkClientException e) {
            log.error("SDK 클라이언트 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("알 수 없는 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

//    /**
//     * 이미지를 최적화하는 메소드.
//     *
//     * @param originalImage 최적화할 원본 이미지
//     * @return 최적화된 이미지
//     * @throws IOException 이미지 최적화 중 발생하는 예외
//     */
//    private BufferedImage optimizeImage(BufferedImage originalImage) throws IOException {
//        int originalWidth = originalImage.getWidth();
//        int originalHeight = originalImage.getHeight();
//
//        // 원하는 최대 크기 설정
//        int maxWidth = (int) (originalWidth * 0.85);
//        int maxHeight = (int) (originalHeight * 0.85);
//
//        // 원본 비율 유지하며 크기 조정
//        double aspectRatio = (double) originalWidth / originalHeight;
//        int newWidth = maxWidth;
//        int newHeight = (int) (maxWidth / aspectRatio);
//        if (newHeight > maxHeight) {
//            newHeight = maxHeight;
//            newWidth = (int) (maxHeight * aspectRatio);
//        }
//
//        // 이미지 리사이즈 및 압축
//        BufferedImage optimizedImage = Thumbnails.of(originalImage)
//                .size(newWidth, newHeight)  // 비율 유지하면서 리사이즈
//                .outputQuality(0.75)  // 이미지 품질 설정 (0.0 ~ 1.0)
//                .asBufferedImage();
//
//        log.info("Original Image Dimensions: {}x{}", originalImage.getWidth(), originalImage.getHeight());
//        log.info("Optimized Image Dimensions: {}x{}", optimizedImage.getWidth(), optimizedImage.getHeight());
//
//
//        return optimizedImage;
//    }

    // 병합 작업 필요 여부를 확인하는 메서드
    public boolean checkIfMergeNeededForGroup(int challengeNum, Lucky lucky, String group) {
        List<SharedFile> sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, lucky);
        long groupFileCount = sharedFiles.stream()
                .filter(file -> {
                    Matcher matcher = Pattern.compile("screenshot_(\\d+)").matcher(file.getFileName());
                    return matcher.find() && matcher.group(1).equals(group);
                })
                .count();

        return groupFileCount >= 4;
    }

    // 병합 작업을 비동기적으로 예약하는 메서드 (특정 그룹)
    @Async
    public CompletableFuture<Void> scheduleMergeImagesForGroup(int challengeNum, Lucky lucky, String group) {
        String key = challengeNum + "_" + lucky.getLuckyId() + "_cut_" + group;

        // 병합 작업이 이미 진행 중인 경우 종료
        if (mergeTasks.putIfAbsent(key, true) != null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture.runAsync(() -> {
            try {
                // 특정 그룹의 모든 이미지가 업로드된 후 병합 수행
                mergeImagesIfNeededForGroup(challengeNum, lucky, group);
            } catch (IOException e) {
                log.error("이미지 병합 작업 실패", e);
            } finally {
                // 병합 작업이 완료되면 플래그를 제거
                mergeTasks.remove(key);
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    // 병합이 필요한 경우 특정 그룹의 이미지를 병합하는 메서드
    @Transactional
    public void mergeImagesIfNeededForGroup(int challengeNum, Lucky lucky, String group) throws IOException {
        List<SharedFile> sharedFiles;

        // 병합 작업 중에는 다른 작업이 접근하지 못하도록 lock 사용
        lock.lock();
        try {
            sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, lucky);
        } finally {
            lock.unlock();
        }

        List<SharedFile> groupFiles = sharedFiles.stream()
                .filter(file -> file.getFileName().contains("screenshot_" + group))
                .toList();

        // 고유 이미지 URL 목록 생성
        Set<String> uniqueImageUrls = groupFiles.stream()
                .map(SharedFile::getFileName)
                .collect(Collectors.toSet());

        // 고유 이미지가 4개 미만이면 병합 작업을 진행하지 않음
        if (uniqueImageUrls.size() < 4) {
            return;
        }

        // 고유 이미지 URL에서 이미지 읽어오기
        List<BufferedImage> selectedImages = uniqueImageUrls.stream()
                .limit(4)
                .map(url -> {
                    try {
                        return ImageIO.read(new URL(url));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        // 필요한 경우, 부족한 이미지 수만큼 빈 이미지 추가
        while (selectedImages.size() < 4) {
            BufferedImage emptyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            selectedImages.add(emptyImage);
        }

        // 병합 이미지 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String baseName = "merged-images/life4cut_" + uuid + "_" + challengeNum + "_" + lucky.getLuckyId() + "_cut_" + group + ".png";
//        String baseName = "merged-images/life4cut_" + challengeNum + "_" + lucky.getLuckyId() + "_cut_" + group + ".png";
        BufferedImage mergedImage = ImageMerger.mergeImages(selectedImages);

        // 병합된 이미지를 S3에 업로드
        String mergedImageUrl = uploadMergedImageToS3(mergedImage, bucket, baseName);

        // 병합된 이미지 정보 저장
        SharedFile mergedFile = new SharedFile();
        mergedFile.setChallengeNum(challengeNum);
        mergedFile.setCreateDate(System.currentTimeMillis());
        mergedFile.setFileName(mergedImageUrl);
        mergedFile.setFileType(FileType.IMAGE);
        mergedFile.setLucky(lucky);

        sharedFileRepository.save(mergedFile);
    }

    // 특정 챌린지와 럭키 번호에 대한 이미지 결과를 가져오는 메서드
    public Map<Integer, List<String>> getFaceChallengeResults(int challengeNum, Long luckyId) {
        List<SharedFile> sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, luckyRepository.getLuckyByLuckyId(luckyId).get());
        Map<Integer, List<String>> groupedFiles = new HashMap<>();
        // 파일 이름 패턴 _cut_${cut_number}
        Pattern pattern = Pattern.compile("_cut_(\\d+)\\.png");
        for (SharedFile sharedFile : sharedFiles) {
            if (sharedFile.getFileName().contains("merged-images")) {
                Matcher matcher = pattern.matcher(sharedFile.getFileName());

                if(matcher.find()) {
                    int cutNumber = Integer.parseInt(matcher.group(1));
                    groupedFiles.computeIfAbsent(cutNumber, k -> new ArrayList<>()).add(sharedFile.getFileName());
                }
            }
        }

        // 결과를 무작위로 선택
        Map<Integer, List<String>> randomResults = new HashMap<>();
        Random random = new Random();

        for (Map.Entry<Integer, List<String>> entry : groupedFiles.entrySet()) {
            List<String> files = entry.getValue();
            if (!files.isEmpty()) {
                String randomFile = files.get(random.nextInt(files.size()));
                randomResults.put(entry.getKey(), Collections.singletonList(randomFile));
            }
        }

        return randomResults;
    }

    @Async
    public void processAndUploadCutAudio(MultipartFile answerFile, String targetWord, Long challengeId, Lucky lucky, Member member, WhisperTranscriptionResponse.word target, Challenge challenge) {
        try {
            // 임시 파일 경로 설정
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Files.createDirectories(tempDir);  // Ensure the directory exists
            Path inputFile = tempDir.resolve(answerFile.getOriginalFilename());
            Files.copy(answerFile.getInputStream(), inputFile, StandardCopyOption.REPLACE_EXISTING); // 임시 저장소로 저장

            // 출력 파일을 저장할 디렉토리 생성
            Path outputDir = tempDir.resolve("split-audio/럭키_" + lucky.getLuckyId());
            Files.createDirectories(outputDir);

            String outputFileName = String.format("멤버_%d_챌린지_%d_%s_VOICE_%d.mp3", member.getMemberId(), challengeId, targetWord, challenge.getChallengeType().getVoiceTypeOrdinal());
            Path outputFile = outputDir.resolve(outputFileName);
            ffmpegService.cutAudioClip(inputFile.toString(), outputFile.toString(), target.getStart(), target.getEnd());

            // 잘린 파일을 MultipartFile로 변환
            MultipartFile multipartFile = new CustomMultipartFile(outputFile.toFile());

            // 잘린 파일을 S3에 업로드
            String s3Path = "split-audio/럭키_" + lucky.getLuckyId() + "/" + outputFileName;
            awsS3Service.uploadAudioFile(multipartFile, s3Path);

            // 임시 파일 삭제
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
        } catch (Exception e) {
            log.error("Error processing and uploading cut audio file", e);
        }
    }


    // 병합한 음성파일 db에 저장
    @Transactional
    public void uploadMergeVoice(Lucky lucky, String voiceUrl, FileType fileType){
        if (fileType != FileType.AUDIO) {
            throw new IllegalArgumentException("Invalid file type: " + fileType);
        }

        SharedFile sharedFile = new SharedFile();
        sharedFile.setFileName(voiceUrl);
        sharedFile.setFileType(FileType.AUDIO);
        sharedFile.setCreateDate(System.currentTimeMillis());
        sharedFile.setLucky(lucky);
        sharedFileRepository.save(sharedFile);
        System.out.println("SharedFile 저장 성공: " + sharedFile.getSharedFileId());
    }

    @Transactional(readOnly = true)
    public SharedFile getMergeVoice(Lucky lucky, FileType fileType) {
        return sharedFileRepository.findByLuckyAndFileType(lucky, fileType);
    }
}