package upscaler.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VideoMetadataTest {
    @Test
    void formatsVideoLabelsPredictably() {
        VideoMetadata metadata = new VideoMetadata(
                Path.of("clip.mp4"),
                1920,
                1080,
                59.94,
                1000,
                125.3,
                "h264",
                true,
                "aac"
        );

        assertEquals("1920 × 1080", metadata.resolutionLabel());
        assertEquals("59.94 fps", metadata.fpsLabel());
        assertEquals("2:05", metadata.durationLabel());
    }
}
