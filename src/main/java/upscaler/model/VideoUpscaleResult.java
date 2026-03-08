package upscaler.model;

import java.nio.file.Path;

public record VideoUpscaleResult(
        Path outputFile,
        VideoMetadata sourceMetadata,
        int outputWidth,
        int outputHeight,
        int outputFrameRate
) {
}
