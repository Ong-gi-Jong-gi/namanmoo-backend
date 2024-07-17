package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.dto.openAI.TranscriptionRequest;
import ongjong.namanmoo.dto.openAI.WhisperTranscriptionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIClientService {

    private final RestTemplate restTemplate;

    @Value("${openai-service.audio-model}")
    private String audioModel;

    @Value("${openai-service.urls.base-url}")
    private String baseUrl;

    @Value("${openai-service.urls.create-transcription-url}")
    private String createTranscriptionUrl;

    public WhisperTranscriptionResponse createTranscription(TranscriptionRequest transcriptionRequest) throws Exception {
        MultipartFile file = transcriptionRequest.getFile();

        // 1. ByteArrayResource 생성
        Resource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        // 2. MultiValueMap 생성 및 데이터 추가
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", audioModel);
        body.add("language", "ko");
        body.add("file", resource);
        body.add("response_format", "verbose_json");
        body.add("timestamp_granularities[]", "word");

        // 3. HTTP 요청 생성 및 전송
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<WhisperTranscriptionResponse> response = restTemplate.postForEntity(
                baseUrl + createTranscriptionUrl, entity, WhisperTranscriptionResponse.class);

        // 4. 응답 처리
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Transcription successful.");
            log.info("Response: {}", response.getBody());
            return response.getBody();
        } else {
            log.error("Error: " + response.getStatusCode());
            log.error("Response: " + response.getBody());
            return null;
        }
    }
}
