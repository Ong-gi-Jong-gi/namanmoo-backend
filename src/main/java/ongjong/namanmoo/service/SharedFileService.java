package ongjong.namanmoo.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import ongjong.namanmoo.domain.*;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.SharedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private final AmazonS3 amazonS3Client;
    private final String bucket;
    private final String region;

    private final ReentrantLock lock = new ReentrantLock();
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

    // 이미지 업로드 메서드
    public Map<String, String> uploadImageFile(Challenge challenge, MultipartFile photo, FileType fileType) throws Exception {
        Map<String, String> response = new HashMap<>();

        Member member = memberService.findMemberByLoginId();
        Family family = member.getFamily();

        if (fileType != FileType.IMAGE) {
            throw new IllegalArgumentException("Invalid file type: " + fileType);
        }

        // S3에 파일 업로드 및 URL 저장
        String uploadedUrl = awsS3Service.uploadFile(photo);

        Optional<Lucky> optionalLucky = luckyRepository.findByFamilyFamilyIdAndRunningTrue(family.getFamilyId());
        Lucky lucky = optionalLucky.orElseThrow(() -> new RuntimeException("Running Lucky not found for family"));

        try {
            // SharedFile 엔티티 저장
            SharedFile sharedFile = new SharedFile();
            sharedFile.setFileName(uploadedUrl);
            sharedFile.setFileType(FileType.IMAGE);
            sharedFile.setChallengeNum(challenge.getChallengeNum());
            sharedFile.setCreateDate(System.currentTimeMillis());
            sharedFile.setLucky(lucky); // Lucky 엔티티 설정
            sharedFileRepository.save(sharedFile);
            // 저장이 성공했는지 확인하는 로그
            System.out.println("SharedFile 저장 성공: " + sharedFile.getSharedFileId());
        } catch (Exception e) {
            e.printStackTrace();  // Or use appropriate logging
        }

        response.put("url", uploadedUrl);
        response.put("message", "Photo uploaded successfully");

        return response;
    }

    public void checkAndMergeImages(int challengeNum, Lucky lucky) throws IOException {
        final int MAX_WAIT_TIME = 15000; // 최대 대기 시간 15초
        final int SLEEP_INTERVAL = 3000; // 3초 간격으로 재시도
        long startTime = System.currentTimeMillis();

        Map<String, List<SharedFile>> groupedFiles = new HashMap<>();
        Pattern pattern = Pattern.compile("screenshot_(\\d+)");

        while (true) {
            List<SharedFile> sharedFiles;

            lock.lock();
            try {
                sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, lucky);
            } finally {
                lock.unlock();
            }

            for (SharedFile sharedFile : sharedFiles) {
                String fileName = sharedFile.getFileName();
                Matcher matcher = pattern.matcher(fileName);

                if (matcher.find()) {
                    String group = matcher.group(1);
                    groupedFiles.computeIfAbsent(group, k -> new ArrayList<>()).add(sharedFile);
                }
            }

            // 모든 그룹에 대해 4개 이상의 이미지를 가진 그룹이 있는지 확인
            boolean allGroupsHaveEnoughImages = groupedFiles.values().stream().allMatch(list -> list.size() >= 4);

            if (allGroupsHaveEnoughImages) {
                break; // 모든 그룹에 4개 이상의 이미지가 있는 경우 병합을 시작
            }

            if (System.currentTimeMillis() - startTime > MAX_WAIT_TIME) {
                throw new IOException("이미지 업로드 대기 시간이 초과되었습니다.");
            }

            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new IOException("병합 대기 중 인터럽트 발생", e);
            }
        }

        // 그룹 키를 정렬하여 순서대로 처리
        List<String> sortedKeys = new ArrayList<>(groupedFiles.keySet());
        Collections.sort(sortedKeys);

        // 각 그룹의 이미지를 BufferedImage 리스트로 변환
        for (String key : sortedKeys) {
            List<SharedFile> sharedFilesInGroup = groupedFiles.get(key);

            // 중복되지 않도록 최대 4개의 고유한 이미지 URL을 선택
            Set<String> uniqueImageUrls = sharedFilesInGroup.stream()
                    .map(SharedFile::getFileName)
                    .collect(Collectors.toSet());

            if (uniqueImageUrls.size() < 4) {
                throw new IOException("충분한 수의 고유 이미지를 찾을 수 없습니다.");
            }

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

            // 빈 공간을 투명하게 채워 4개가 되도록 처리
            while (selectedImages.size() < 4) {
                BufferedImage emptyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB); // 너비와 높이를 100으로 설정하여 빈 이미지를 추가
                selectedImages.add(emptyImage);
            }

            // UUID 생성 및 파일 이름 설정
            String uuid = UUID.randomUUID().toString();
            String baseName = "merged-images/" + uuid + "_" + challengeNum + "_" + lucky.getLuckyId() + "_cut_" + key + ".png";
            BufferedImage mergedImage = ImageMerger.mergeImages(selectedImages); // 이미지를 병합하여 새로운 BufferedImage를 생성

            // 병합된 이미지를 S3에 업로드
            String mergedImageUrl = uploadMergedImageToS3(mergedImage, bucket, baseName);

            // 병합된 이미지 URL을 데이터베이스에 저장
            SharedFile mergedFile = new SharedFile();
            mergedFile.setChallengeNum(challengeNum);
            mergedFile.setCreateDate(System.currentTimeMillis());
            mergedFile.setFileName(mergedImageUrl);
            mergedFile.setFileType(FileType.IMAGE);
            mergedFile.setLucky(lucky);

            sharedFileRepository.save(mergedFile); // 데이터베이스에 새 SharedFile 저장
        }
    }

//    // TODO: 방법 1: (동기) 이미지 업로드와 병합 분리
//    // 병합을 수행하는 메소드
//    public void mergeImagesIfNeeded(int challengeNum, Lucky lucky) throws IOException {
//        final int MAX_WAIT_TIME = 15000; // 최대 대기 시간 15초
//        final int SLEEP_INTERVAL = 3000; // 3초 간격으로 재시도
//        long startTime = System.currentTimeMillis();
//
//        Map<String, List<SharedFile>> groupedFiles = new HashMap<>();
//        Pattern pattern = Pattern.compile("screenshot_(\\d+)");
//
//        while (true) {
//            List<SharedFile> sharedFiles;
//
//            lock.lock();
//            try {
//                sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, lucky);
//            } finally {
//                lock.unlock();
//            }
//
//            for (SharedFile sharedFile : sharedFiles) {
//                String fileName = sharedFile.getFileName();
//                Matcher matcher = pattern.matcher(fileName);
//
//                if (matcher.find()) {
//                    String group = matcher.group(1);
//                    groupedFiles.computeIfAbsent(group, k -> new ArrayList<>()).add(sharedFile);
//                }
//            }
//
//            // 모든 그룹에 대해 4개 이상의 이미지를 가진 그룹이 있는지 확인
//            boolean allGroupsHaveEnoughImages = groupedFiles.values().stream().allMatch(list -> list.size() >= 4);
//
//            if (allGroupsHaveEnoughImages) {
//                break; // 모든 그룹에 4개 이상의 이미지가 있는 경우 병합을 시작
//            }
//            if (System.currentTimeMillis() - startTime > MAX_WAIT_TIME) {
//                throw new IOException("이미지 업로드 대기 시간이 초과되었습니다.");
//            }
//            try {
//                Thread.sleep(SLEEP_INTERVAL);
//            } catch (InterruptedException e) {
//                throw new IOException("병합 대기 중 인터럽트 발생", e);
//            }
//        }
//
//        // 그룹 키를 정렬하여 순서대로 처리
//        List<String> sortedKeys = new ArrayList<>(groupedFiles.keySet());
//        Collections.sort(sortedKeys);
//
//        // 각 그룹의 이미지를 BufferedImage 리스트로 변환하고 병합
//        for (String key : sortedKeys) {
//            List<SharedFile> sharedFilesInGroup = groupedFiles.get(key);
//
//            Set<String> uniqueImageUrls = sharedFilesInGroup.stream()
//                    .map(SharedFile::getFileName)
//                    .collect(Collectors.toSet());
//
//            if (uniqueImageUrls.size() < 4) {
//                throw new IOException("충분한 수의 고유 이미지를 찾을 수 없습니다.");
//            }
//
//            List<BufferedImage> selectedImages = uniqueImageUrls.stream()
//                    .limit(4)
//                    .map(url -> {
//                        try {
//                            return ImageIO.read(new URL(url));
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                    })
//                    .collect(Collectors.toList());
//
//            // 빈 공간을 투명하게 채워 4개가 되도록 처리
//            while (selectedImages.size() < 4) {
//                BufferedImage emptyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
//                selectedImages.add(emptyImage);
//            }
//
//            // UUID 생성 및 파일 이름 설정
//            String uuid = UUID.randomUUID().toString();
//            String baseName = "merged-images/" + uuid + "_" + challengeNum + "_" + lucky.getLuckyId() + "_cut_" + key + ".png";
//            BufferedImage mergedImage = ImageMerger.mergeImages(selectedImages);
//
//            // 병합된 이미지를 S3에 업로드
//            String mergedImageUrl = uploadMergedImageToS3(mergedImage, bucket, baseName);
//
//            // 병합된 이미지 URL을 데이터베이스에 저장
//            SharedFile mergedFile = new SharedFile();
//            mergedFile.setChallengeNum(challengeNum);
//            mergedFile.setCreateDate(System.currentTimeMillis());
//            mergedFile.setFileName(mergedImageUrl);
//            mergedFile.setFileType(FileType.IMAGE);
//            mergedFile.setLucky(lucky);
//
//            sharedFileRepository.save(mergedFile); // 데이터베이스에 새 SharedFile 저장
//
//            // 디버깅 로그 추가
//            System.out.println("Merged image saved: " + mergedImageUrl);
//        }
//    }

//    // TODO: 방법 2: (비동기) 병합을 서버 측에서 스케줄링
//    // 이미지 업로드 후 병합을 예약하는 메소드
//    public void scheduleMergeImages(int challengeNum, Lucky lucky) {
//        // 이미지 업로드 후 병합 예약
//        CompletableFuture.supplyAsync(() -> {
//            try {
//                // 이미지 업로드가 완료될 때까지 대기
//                waitForImageUploadCompletion(challengeNum, lucky);
//
//                // 모든 이미지가 업로드된 후 병합 수행
//                mergeImagesIfNeeded(challengeNum, lucky);
//            } catch (IOException e) {
//                // 예외 처리 로직 추가
//                e.printStackTrace();
//            }
//            return null;
//        });
//    }

    // 병합 작업을 비동기적으로 예약하는 메서드
    @Async
    public CompletableFuture<Void> scheduleMergeImages(int challengeNum, Lucky lucky) {
        String key = challengeNum + "_" + lucky.getLuckyId();

        // 병합 작업이 이미 진행 중인 경우 종료
        if (mergeTasks.putIfAbsent(key, true) != null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture.runAsync(() -> {
            try {
                waitForImageUploadCompletion(challengeNum, lucky);
                mergeImagesIfNeeded(challengeNum, lucky);
            } catch (IOException e) {
                e.printStackTrace();  // Or use appropriate logging
            } finally {
                // 병합 작업이 완료되면 플래그를 제거
                mergeTasks.remove(key);
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    // 이미지 업로드가 완료될 때까지 대기하는 메소드
    private void waitForImageUploadCompletion(int challengeNum, Lucky lucky) throws IOException {
        final int MAX_WAIT_TIME = 15000; // 최대 대기 시간 15초
        final int SLEEP_INTERVAL = 3000; // 3초 간격으로 재시도
        long startTime = System.currentTimeMillis();

        while (true) {
            List<SharedFile> sharedFiles;

            lock.lock();
            try {
                sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, lucky);
            } finally {
                lock.unlock();
            }

            // 모든 그룹에 대해 4개 이상의 이미지를 가진 그룹이 있는지 확인
            boolean allGroupsHaveEnoughImages = sharedFiles.stream()
                    .map(SharedFile::getFileName)
                    .filter(fileName -> fileName.startsWith("screenshot_"))
                    .collect(Collectors.groupingBy(fileName -> {
                        Matcher matcher = Pattern.compile("screenshot_(\\d+)").matcher(fileName);
                        return matcher.find() ? matcher.group(1) : "";
                    }))
                    .values().stream()
                    .allMatch(list -> list.size() >= 4);

            if (allGroupsHaveEnoughImages) {
                break; // 모든 그룹에 4개 이상의 이미지가 있는 경우 대기 종료
            }

            if (System.currentTimeMillis() - startTime > MAX_WAIT_TIME) {
                throw new IOException("이미지 업로드 대기 시간이 초과되었습니다.");
            }

            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new IOException("병합 대기 중 인터럽트 발생", e);
            }
        }
    }

    // 병합이 필요한 경우 이미지를 병합하는 메서드
    private void mergeImagesIfNeeded(int challengeNum, Lucky lucky) throws IOException {
        Map<String, List<SharedFile>> groupedFiles = new HashMap<>();
        Pattern pattern = Pattern.compile("screenshot_(\\d+)");

        List<SharedFile> sharedFiles;

        lock.lock();
        try {
            sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, lucky);
        } finally {
            lock.unlock();
        }

        for (SharedFile sharedFile : sharedFiles) {
            String fileName = sharedFile.getFileName();
            Matcher matcher = pattern.matcher(fileName);

            if (matcher.find()) {
                String group = matcher.group(1);
                groupedFiles.computeIfAbsent(group, k -> new ArrayList<>()).add(sharedFile);
            }
        }

        List<String> sortedKeys = new ArrayList<>(groupedFiles.keySet());
        Collections.sort(sortedKeys);

        for (String key : sortedKeys) {
            List<SharedFile> sharedFilesInGroup = groupedFiles.get(key);

            Set<String> uniqueImageUrls = sharedFilesInGroup.stream()
                    .map(SharedFile::getFileName)
                    .collect(Collectors.toSet());

            if (uniqueImageUrls.size() < 4) {
                throw new IOException("충분한 수의 고유 이미지를 찾을 수 없습니다.");
            }

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

            while (selectedImages.size() < 4) {
                BufferedImage emptyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
                selectedImages.add(emptyImage);
            }

            String uuid = UUID.randomUUID().toString();
            String baseName = "merged-images/" + uuid + "_" + challengeNum + "_" + lucky.getLuckyId() + "_cut_" + key + ".png";
            BufferedImage mergedImage = ImageMerger.mergeImages(selectedImages);

            String mergedImageUrl = uploadMergedImageToS3(mergedImage, bucket, baseName);

            SharedFile mergedFile = new SharedFile();
            mergedFile.setChallengeNum(challengeNum);
            mergedFile.setCreateDate(System.currentTimeMillis());
            mergedFile.setFileName(mergedImageUrl);
            mergedFile.setFileType(FileType.IMAGE);
            mergedFile.setLucky(lucky);

            sharedFileRepository.save(mergedFile);
        }
    }

    // 병합된 이미지를 S3에 업로드하는 메서드
    public String uploadMergedImageToS3(BufferedImage mergedImage, String bucketName, String fileObjKeyName) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(mergedImage, "png", os);
        byte[] buffer = os.toByteArray();
        InputStream is = new ByteArrayInputStream(buffer);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(buffer.length);
        meta.setContentType("image/png");

        amazonS3Client.putObject(new PutObjectRequest(bucketName, fileObjKeyName, is, meta).withCannedAcl(CannedAccessControlList.PublicRead));

        // 선택: amazonS3Client.getUrl(bucketName, fileObjKeyName).toString(); 또는 String.format을 사용
        return amazonS3Client.getUrl(bucketName, fileObjKeyName).toString();
        // 또는
        // return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileObjKeyName);
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

}
