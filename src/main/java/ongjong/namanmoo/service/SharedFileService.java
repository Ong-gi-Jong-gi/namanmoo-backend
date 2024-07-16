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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
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

    // 화상통화 챌린지 이미지 4장 병합 및 저장
    public void checkAndMergeImages(int challengeNum, Lucky lucky) throws IOException {
        List<SharedFile> sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, lucky);

        // 파일 이름의 숫자를 기준으로 그룹화
        Map<String, List<SharedFile>> groupedFiles = new HashMap<>();
        Pattern pattern = Pattern.compile("screenshot_(\\d+)");

        for (SharedFile sharedFile : sharedFiles) {
            String fileName = sharedFile.getFileName();
            Matcher matcher = pattern.matcher(fileName);

            if (matcher.find()) {
                String group = matcher.group(1);
                groupedFiles.computeIfAbsent(group, k -> new ArrayList<>()).add(sharedFile);
            }
        }

        // 그룹 키를 정렬하여 순서대로 처리
        List<String> sortedKeys = new ArrayList<>(groupedFiles.keySet());
        Collections.sort(sortedKeys);

        // 각 그룹의 이미지를 BufferedImage 리스트로 변환
        for (String key : sortedKeys) {
            List<BufferedImage> images = groupedFiles.get(key).stream()
                    .map(sharedFile -> {
                        try {
                            return ImageIO.read(new URL(sharedFile.getFileName()));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            // 빈 공간을 투명하게 채워 4개가 되도록 처리
            while (images.size() < 4) {
                BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                images.add(emptyImage);
            }

            // UUID 생성 및 파일 이름 설정
            String uuid = UUID.randomUUID().toString();
            String baseName = "merged-images/" + uuid + "_" + challengeNum + "_" + lucky.getLuckyId() + "_cut_" + key + ".png";
            BufferedImage mergedImage = ImageMerger.mergeImages(images); // 이미지를 병합하여 새로운 BufferedImage를 생성

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
    public List<String> getFaceChallengeResults(int challengeNum, Long luckyId) {
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

        // 결과를 무작위로 선택하는 대신 모든 URL을 반환하도록 수정
        return groupedFiles;
    }

}
