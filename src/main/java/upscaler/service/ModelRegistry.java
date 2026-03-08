package upscaler.service;

import upscaler.config.AppDirectories;
import upscaler.model.ModelDefinition;
import upscaler.model.ModelSource;
import upscaler.runtime.PreparedRuntime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class ModelRegistry {
    private static final List<ModelDefinition> BUNDLED_MODELS = List.of(
            new ModelDefinition(
                    "realesrgan-x4plus",
                    "Real-ESRGAN General",
                    "Balanced model for photos, textures, scans and general image restoration.",
                    ModelSource.BUNDLED
            ),
            new ModelDefinition(
                    "realesrgan-x4plus-anime",
                    "Real-ESRGAN Anime",
                    "Sharper, more stylized reconstruction tuned for anime and illustration.",
                    ModelSource.BUNDLED
            )
    );

    public List<ModelDefinition> listModels() {
        List<ModelDefinition> models = new ArrayList<>(BUNDLED_MODELS);
        models.addAll(discoverUserModels());
        models.sort(Comparator.comparing(ModelDefinition::displayName, String.CASE_INSENSITIVE_ORDER));
        return models;
    }

    public Optional<ModelDefinition> findByName(String name) {
        return listModels().stream()
                .filter(model -> model.name().equals(name))
                .findFirst();
    }

    public Path resolveModelDirectory(ModelDefinition model, PreparedRuntime runtime) {
        return model.source() == ModelSource.BUNDLED
                ? runtime.modelsDirectory()
                : AppDirectories.userModelsDirectory();
    }

    private List<ModelDefinition> discoverUserModels() {
        try {
            AppDirectories.ensureLayout();
        } catch (IOException ignored) {
            return List.of();
        }

        List<ModelDefinition> models = new ArrayList<>();
        try (Stream<Path> stream = Files.list(AppDirectories.userModelsDirectory())) {
            stream.filter(path -> path.getFileName().toString().endsWith(".bin"))
                    .sorted()
                    .forEach(binFile -> {
                        String name = stripExtension(binFile.getFileName().toString());
                        Path paramFile = AppDirectories.userModelsDirectory().resolve(name + ".param");
                        if (Files.exists(paramFile)) {
                            models.add(new ModelDefinition(
                                    name,
                                    buildDisplayName(name),
                                    "Custom NCNN model loaded from your Upscaler models folder.",
                                    ModelSource.USER
                            ));
                        }
                    });
        } catch (IOException ignored) {
            return List.of();
        }
        return models;
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    private String buildDisplayName(String name) {
        String[] parts = name.replace('_', '-').split("-");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            String normalized = part.toLowerCase(Locale.ROOT);
            if (normalized.equals("x4plus") || normalized.equals("x2") || normalized.equals("x3") || normalized.equals("x4")) {
                builder.append(part.toUpperCase(Locale.ROOT));
            } else {
                builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return builder + " (Custom)";
    }
}
