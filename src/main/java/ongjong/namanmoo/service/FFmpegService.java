package ongjong.namanmoo.service;


import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class FFmpegService {

    private final FFmpegExecutor ffmpegExecutor;

    public FFmpegService(FFmpegExecutor ffmpegExecutor) {
        this.ffmpegExecutor = ffmpegExecutor;
    }

    // 오디오 클립 자르는 메서드
    public void cutAudioClip(String inputFilePath, String outputFilePath, double startTime, double endTime) {
        // 밀리초 단위로 변경
        long startMillis = (long) (startTime * 1000);
        long endMillis = (long) (endTime * 1000);
        long durationMillis = endMillis - startMillis;

        // 자를 오디오 클립 작업을 빌드
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputFilePath)
                .addOutput(outputFilePath)
                .setStartOffset(startMillis, TimeUnit.MILLISECONDS)
                .setDuration(durationMillis, TimeUnit.MILLISECONDS)
                .done();

        // 작업 실행
        ffmpegExecutor.createJob(builder).run();
    }

    // 파일을 MP3 형식으로 변환하는 메서드
    private String convertToMp3(String inputFilePath) throws IOException {
        if (inputFilePath.endsWith(".mp3")) {
            return inputFilePath; // 이미 mp3인 경우 변환하지 않음
        }

        //확장자를 .mp3으로 변경
        String outputFilePath = inputFilePath.replaceAll("\\.[^.]+$", ".mp3");
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputFilePath)
                .addOutput(outputFilePath)
                .setFormat("mp3")
                .setAudioCodec("libmp3lame")
                .done();

        ffmpegExecutor.createJob(builder).run();
        return outputFilePath;
    }


    public void mergeAudiosWithSilence(List<String> inputPaths, String outputPath, double silenceDuration) {
        // 모든 입력파일 mp3 파일로 변경
        List<String> mp3Paths = inputPaths.stream()
                .map(path -> {
                    try {
                        return convertToMp3(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        // 빌더 초기화
        FFmpegBuilder builder = new FFmpegBuilder();

        // 입력 파일들의 경로를 빌더에 입력으로 추가
        inputPaths.forEach(builder::addInput);

        // 오디오 사이에 침묵구간 추가
        String filterComplex = IntStream.range(0, mp3Paths.size())
                .mapToObj(i -> String.format("[%d:a]adelay=%d|%d[s%d]", i, (int)(i * silenceDuration * 1000), (int)(i * silenceDuration * 1000), i))
                .collect(Collectors.joining("; ")) +
                String.format("; %sconcat=n=%d:v=0:a=1[a]",
                        IntStream.range(0, mp3Paths.size()).mapToObj(i -> "[s" + i + "]").collect(Collectors.joining()),
                        mp3Paths.size());

        // 빌더에 filterComplex 설정 및 출력 설정
        builder.setComplexFilter(filterComplex)
                .addOutput(outputPath)
                .setAudioCodec("libmp3lame")
                .addExtraArgs("-map", "[a]")
                .done();

        log.info("Executing FFmpeg command with filter: {}", filterComplex);
        ffmpegExecutor.createJob(builder).run();
    }
}