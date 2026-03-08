package upscaler.runtime;

import java.nio.file.Path;

public record PreparedRuntime(
        Platform platform,
        Path rootDirectory,
        Path executable,
        Path modelsDirectory
) {
}
