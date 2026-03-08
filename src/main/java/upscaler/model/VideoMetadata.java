package upscaler.model;

import java.nio.file.Path;

public record VideoMetadata(
        Path file,
        int width,
        int height,
        double frameRate,
        long estimatedFrameCount,
        double durationSeconds,
        String videoCodec,
        boolean hasAudio,
        String audioCodec
) {
    public String resolutionLabel() {
        return width + " × " + height;
    }

    public String fpsLabel() {
        return frameRate <= 0 ? "Unknown FPS" : String.format("%.2f fps", frameRate);
    }

    public String durationLabel() {
        if (durationSeconds <= 0) {
            return "Unknown duration";
        }
        long totalSeconds = Math.round(durationSeconds);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }
}
