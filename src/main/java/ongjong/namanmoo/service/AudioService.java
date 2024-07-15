//package ongjong.namanmoo.service;
//
//import net.bramp.ffmpeg.FFmpegExecutor;
//import net.bramp.ffmpeg.builder.FFmpegBuilder;
//import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//@Configuration
//public class AudioService {
//
//    private final FFmpegExecutor ffmpegExecutor;
//
//    @Autowired
//    public AudioService(FFmpegExecutor ffmpegExecutor) {
//        this.ffmpegExecutor = ffmpegExecutor;
//    }
//
//    public void trimAudio(String inputPath, String outputPath, double startTime, double duration) throws IOException {
//
//        long startMillis = (long) (startTime * 1000); // seconds to milliseconds
//        long durationMillis = (long) (duration * 1000); // seconds to milliseconds
//
//        FFmpegBuilder builder = new FFmpegBuilder()
//                .setInput(inputPath)
//                .addOutput(outputPath)
//                .setStartOffset(startMillis, TimeUnit.MILLISECONDS)
//                .setDuration(durationMillis, TimeUnit.MILLISECONDS)
//                .done();
//
//        ffmpegExecutor.createJob(builder).run();
////        return awsS3Service.uploadFile(new File(outputPath));
//    }
//
//
//    public void mergeAudiosWithSilence(List<String> inputPaths, String outputPath, double silenceDuration) throws IOException {
//        FFmpegBuilder builder = new FFmpegBuilder();
//
//        for (String inputPath : inputPaths) {
//            builder.addInput(inputPath);
//        }
//
//        // 각 파일 사이에 silenceDuration 만큼의 지연 시간을 추가
//        String filterComplex = IntStream.range(0, inputPaths.size())
//                .mapToObj(i -> String.format("[%d:a]adelay=%d|%d[s%d]", i, (int)(i * silenceDuration * 1000), (int)(i * silenceDuration * 1000), i))
//                .collect(Collectors.joining("; ")) +
//                String.format("; %s concat=n=%d:v=0:a=1[out]",
//                        IntStream.range(0, inputPaths.size())
//                                .mapToObj(i -> "[s" + i + "]")
//                                .collect(Collectors.joining("")),
//                        inputPaths.size());
//
//        builder.setComplexFilter(filterComplex)
//                .addOutput(outputPath)
//                .addExtraArgs("-map", "[out]")
//                .done();
//
//        ffmpegExecutor.createJob(builder).run();
//    }
//
//    public void mergeAudios(List<String> inputPaths, String outputPath) throws IOException {
//        FFmpegBuilder builder = new FFmpegBuilder();
//
//        for (String inputPath : inputPaths) {
//            builder.addInput(inputPath);
//        }
//
//        String filterComplex = "concat=n=" + inputPaths.size() + ":v=0:a=1[out]";
//        builder.setComplexFilter(filterComplex)
//                .addOutput(outputPath)
//                .addExtraArgs("-map", "[out]")
//                .done();
//
//        ffmpegExecutor.createJob(builder).run();
//    }
//}
