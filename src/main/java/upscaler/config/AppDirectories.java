package upscaler.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AppDirectories {
    private static final String APP_NAME = "Upscaler";

    private AppDirectories() {
    }

    public static Path appHome() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", ".");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Path.of(appData, APP_NAME);
            }
        }

        if (os.contains("mac")) {
            return Path.of(home, "Library", "Application Support", APP_NAME);
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return Path.of(xdgDataHome, APP_NAME);
        }

        return Path.of(home, ".local", "share", APP_NAME);
    }

    public static Path runtimeDirectory() {
        return appHome().resolve("runtime");
    }

    public static Path userModelsDirectory() {
        return appHome().resolve("models");
    }

    public static Path settingsFile() {
        return appHome().resolve("settings.properties");
    }

    public static void ensureLayout() throws IOException {
        Files.createDirectories(appHome());
        Files.createDirectories(runtimeDirectory());
        Files.createDirectories(userModelsDirectory());
    }
}
