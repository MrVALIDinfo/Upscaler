package upscaler.service;

import upscaler.model.VideoMetadata;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VideoMetadataService {
    private final VideoToolingService toolingService;

    public VideoMetadataService(VideoToolingService toolingService) {
        this.toolingService = toolingService;
    }

    public VideoMetadata probe(Path videoFile) throws IOException {
        toolingService.requireVideoTools();
        Map<String, String> videoData = runProbe(
                "ffprobe", "-v", "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height,avg_frame_rate,r_frame_rate,codec_name,duration,nb_frames",
                "-of", "default=noprint_wrappers=1", videoFile.toString()
        );
        Map<String, String> formatData = runProbe(
                "ffprobe", "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1", videoFile.toString()
        );
        Map<String, String> audioData = runProbe(
                "ffprobe", "-v", "error", "-select_streams", "a:0",
                "-show_entries", "stream=codec_name",
                "-of", "default=noprint_wrappers=1", videoFile.toString()
        );

        int width = parseInt(videoData.get("width"), 0);
        int height = parseInt(videoData.get("height"), 0);
        double frameRate = parseFrameRate(videoData.getOrDefault("avg_frame_rate", videoData.get("r_frame_rate")));
        double duration = parseDouble(videoData.get("duration"), parseDouble(formatData.get("duration"), 0d));
        long frameCount = parseLong(videoData.get("nb_frames"), frameRate > 0 && duration > 0 ? Math.round(frameRate * duration) : 0L);
        String videoCodec = videoData.getOrDefault("codec_name", "unknown");
        boolean hasAudio = audioData.containsKey("codec_name");
        String audioCodec = audioData.getOrDefault("codec_name", hasAudio ? "unknown" : "none");

        return new VideoMetadata(videoFile, width, height, frameRate, frameCount, duration, videoCodec, hasAudio, audioCodec);
    }

    public BufferedImage extractThumbnail(VideoMetadata metadata) throws IOException {
        toolingService.requireVideoTools();
        Path tempDir = Files.createTempDirectory("upscaler-thumb-");
        Path thumb = tempDir.resolve("thumb.png");
        double seekSeconds = metadata.durationSeconds() > 3 ? Math.min(1.5d, metadata.durationSeconds() / 3d) : 0d;

        Process process = new ProcessBuilder(
                "ffmpeg", "-y",
                "-ss", String.format(java.util.Locale.US, "%.2f", seekSeconds),
                "-i", metadata.file().toString(),
                "-frames:v", "1",
                thumb.toString()
        ).redirectErrorStream(true).start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                // discard ffmpeg output for thumbnail extraction
            }
        }

        try {
            process.waitFor(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thumbnail extraction interrupted", e);
        }

        if (process.exitValue() != 0 || Files.notExists(thumb)) {
            throw new IOException("Failed to extract video thumbnail.");
        }

        try {
            BufferedImage image = ImageIO.read(thumb.toFile());
            if (image == null) {
                throw new IOException("ffmpeg created an unreadable thumbnail image.");
            }
            return image;
        } finally {
            Files.deleteIfExists(thumb);
            Files.deleteIfExists(tempDir);
        }
    }

    private Map<String, String> runProbe(String... command) throws IOException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        Map<String, String> values = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("=")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                values.put(parts[0].trim(), parts[1].trim());
            }
        }
        try {
            process.waitFor(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ffprobe interrupted", e);
        }
        if (process.exitValue() != 0) {
            throw new IOException("ffprobe failed for command: " + String.join(" ", command));
        }
        return values;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double parseFrameRate(String value) {
        if (value == null || value.isBlank()) {
            return 0d;
        }
        if (!value.contains("/")) {
            return parseDouble(value, 0d);
        }
        String[] parts = value.split("/", 2);
        double numerator = parseDouble(parts[0], 0d);
        double denominator = parseDouble(parts[1], 1d);
        if (denominator == 0d) {
            return 0d;
        }
        return numerator / denominator;
    }
}
