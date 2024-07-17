package ongjong.namanmoo.service;


import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class FFmpegService {

    private final FFmpegExecutor ffmpegExecutor;

    public FFmpegService(FFmpegExecutor ffmpegExecutor) {
        this.ffmpegExecutor = ffmpegExecutor;
    }

    public void cutAudioClip(String inputFilePath, String outputFilePath, double startTime, double endTime) {

        long startMillis = (long) (startTime * 1000);
        long endMillis = (long) (endTime * 1000);
        long durationMillis = endMillis - startMillis;

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputFilePath)
                .addOutput(outputFilePath)
                .setStartOffset(startMillis, TimeUnit.MILLISECONDS)
                .setDuration(durationMillis, TimeUnit.MILLISECONDS)
                .done();

        ffmpegExecutor.createJob(builder).run();
    }
}