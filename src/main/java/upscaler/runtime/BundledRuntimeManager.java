package upscaler.runtime;

import upscaler.config.AppDirectories;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class BundledRuntimeManager {
    private static final List<String> MODEL_FILES = List.of(
            "realesrgan-x4plus.bin",
            "realesrgan-x4plus.param",
            "realesrgan-x4plus-anime.bin",
            "realesrgan-x4plus-anime.param"
    );

    private final String appVersion;

    public BundledRuntimeManager(String appVersion) {
        this.appVersion = appVersion;
    }

    public PreparedRuntime prepareRuntime() throws IOException {
        AppDirectories.ensureLayout();
        Platform platform = PlatformDetector.detect();
        Path runtimeRoot = AppDirectories.runtimeDirectory().resolve(platform.resourceFolder());
        Path versionFile = runtimeRoot.resolve(".version");

        if (Files.notExists(runtimeRoot) || Files.notExists(versionFile) || !appVersion.equals(Files.readString(versionFile))) {
            recreateRuntime(runtimeRoot, platform, versionFile);
        }

        Path executable = runtimeRoot.resolve(platform.executableName());
        Path modelsDirectory = runtimeRoot.resolve("models");
        if (!Files.isExecutable(executable)) {
            executable.toFile().setExecutable(true, false);
        }
        return new PreparedRuntime(platform, runtimeRoot, executable, modelsDirectory);
    }

    private void recreateRuntime(Path runtimeRoot, Platform platform, Path versionFile) throws IOException {
        deleteDirectoryIfExists(runtimeRoot);
        Files.createDirectories(runtimeRoot.resolve("models"));

        for (String modelFile : MODEL_FILES) {
            copyResource("/runtime/realesrgan/common/models/" + modelFile, runtimeRoot.resolve("models").resolve(modelFile));
        }

        copyResource(
                "/runtime/realesrgan/" + platform.resourceFolder() + "/" + platform.executableName(),
                runtimeRoot.resolve(platform.executableName())
        );

        for (String file : platform.sidecarFiles()) {
            copyResource("/runtime/realesrgan/" + platform.resourceFolder() + "/" + file, runtimeRoot.resolve(file));
        }

        runtimeRoot.resolve(platform.executableName()).toFile().setExecutable(true, false);
        Files.writeString(versionFile, appVersion);
    }

    private void copyResource(String resourcePath, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        try (InputStream input = BundledRuntimeManager.class.getResourceAsStream(resourcePath)) {
            Objects.requireNonNull(input, "Missing bundled resource: " + resourcePath);
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteDirectoryIfExists(Path directory) throws IOException {
        if (Files.notExists(directory)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + path, e);
                        }
                    });
        }
    }
}
