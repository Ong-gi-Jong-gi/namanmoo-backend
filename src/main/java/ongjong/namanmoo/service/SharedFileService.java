package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.*;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.SharedFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SharedFileService {

    private final SharedFileRepository sharedFileRepository;
    private final AwsS3Service awsS3Service;
    private final LuckyRepository luckyRepository;
    private final MemberService memberService;

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

        // SharedFile 엔티티 저장
        SharedFile sharedFile = new SharedFile();
        sharedFile.setFileName(uploadedUrl);
        sharedFile.setFileType(FileType.IMAGE);
        sharedFile.setChallengeNum(challenge.getChallengeNum());
        sharedFile.setCreationDate(System.currentTimeMillis());
        sharedFile.setLucky(lucky); // Lucky 엔티티 설정
        sharedFileRepository.save(sharedFile);

        response.put("url", uploadedUrl);
        response.put("message", "Photo uploaded successfully");

        return response;
    }

    public Map<String, BufferedImage> getChallengeResults(int challengeNum, Long luckyId) throws IOException {
        Lucky lucky = luckyRepository.getLuckyByLuckyId(luckyId);
        List<SharedFile> sharedFiles = sharedFileRepository.findByChallengeNumAndLucky(challengeNum, lucky);

        if (sharedFiles.isEmpty()) {
            System.out.println("No files found for the given challengeNum and luckyId");
            return Collections.emptyMap();
        } else {
            System.out.println("Found " + sharedFiles.size() + " files.");
        }

        // 파일 이름의 숫자를 기준으로 그룹화
        Map<String, List<SharedFile>> groupedFiles = new HashMap<>();
        Pattern pattern = Pattern.compile("screenshot_(\\d+)");

        for (SharedFile sharedFile : sharedFiles) {
            String fileName = sharedFile.getFileName();
            System.out.println("Processing file: " + fileName);
            Matcher matcher = pattern.matcher(fileName);

            if (matcher.find()) {
                String group = matcher.group(1);
                groupedFiles.computeIfAbsent(group, k -> new ArrayList<>()).add(sharedFile);
                System.out.println("Matched group: " + group);
            } else {
                System.out.println("No match found for file: " + fileName);
            }
        }

        Map<String, BufferedImage> mergedImages = new HashMap<>();

        for (Map.Entry<String, List<SharedFile>> entry : groupedFiles.entrySet()) {
            List<File> imageFiles = entry.getValue().stream()
                    .map(sharedFile -> new File(sharedFile.getFileName()))
                    .collect(Collectors.toList());

            if (imageFiles.size() == 4) {
                BufferedImage mergedImage = ImageMerger.mergeImages(imageFiles);
                mergedImages.put(entry.getKey(), mergedImage);
            }
        }

        return mergedImages;
    }

}
