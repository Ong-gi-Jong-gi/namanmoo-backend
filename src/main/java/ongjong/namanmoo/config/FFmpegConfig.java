package ongjong.namanmoo.config;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FFmpegConfig {

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${ffprobe.path}")
    private String ffprobePath;

    @Bean
    public FFmpeg ffMpeg() throws Exception {
        return new FFmpeg(ffmpegPath); // 기본 생성자 사용
    }

    @Bean
    public FFprobe ffProbe() throws Exception {
        return new FFprobe(ffprobePath); // 기본 생성자 사용
    }

    @Bean
    public FFmpegExecutor ffExecutor(FFmpeg ffMpeg, FFprobe ffProbe) {
        return new FFmpegExecutor(ffMpeg, ffProbe);
    }
}