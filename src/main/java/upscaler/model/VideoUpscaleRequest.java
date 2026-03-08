package upscaler.model;

import java.nio.file.Path;

public record VideoUpscaleRequest(
        Path inputFile,
        Path outputFile,
        ModelDefinition model,
        int scaleFactor,
        ScalePlan scalePlan,
        int targetFrameRate,
        VideoQualityPreset qualityPreset,
        String videoEncoderId,
        String engineGpuId,
        int tileSize,
        String threadProfile,
        boolean ttaEnabled
) {
}
