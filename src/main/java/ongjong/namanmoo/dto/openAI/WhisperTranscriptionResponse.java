package ongjong.namanmoo.dto.openAI;

import lombok.Data;

import java.util.List;

@Data
public class WhisperTranscriptionResponse {
    private String text;
    private List<word> words;

    @Data
    public static class word {
        private String word;
        private double start;
        private double end;
    }
}