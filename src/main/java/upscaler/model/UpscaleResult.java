package upscaler.model;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

public record UpscaleResult(
        Path outputFile,
        BufferedImage image,
        List<String> commandHistory
) {
}
