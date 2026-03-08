package upscaler.model;

import java.nio.file.Path;

public record UpscaleRequest(
        Path inputFile,
        ModelDefinition model,
        ScalePlan scalePlan,
        String engineGpuId,
        int tileSize,
        String threadProfile,
        boolean ttaEnabled
) {
}
