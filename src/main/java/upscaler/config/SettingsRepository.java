package upscaler.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SettingsRepository {
    public AppSettings load() {
        AppSettings settings = new AppSettings();
        Path file = AppDirectories.settingsFile();

        if (!Files.exists(file)) {
            settings.normalize();
            return settings;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            settings.setSelectedModel(properties.getProperty("selectedModel", settings.getSelectedModel()));
            settings.setScaleFactor(Integer.parseInt(properties.getProperty("scaleFactor", Integer.toString(settings.getScaleFactor()))));
            settings.setDeviceSelection(properties.getProperty("deviceSelection", settings.getDeviceSelection()));
            settings.setTileSize(Integer.parseInt(properties.getProperty("tileSize", Integer.toString(settings.getTileSize()))));
            settings.setThreadProfile(properties.getProperty("threadProfile", settings.getThreadProfile()));
            settings.setTtaEnabled(Boolean.parseBoolean(properties.getProperty("ttaEnabled", Boolean.toString(settings.isTtaEnabled()))));
            settings.setLastInputDirectory(properties.getProperty("lastInputDirectory", settings.getLastInputDirectory()));
            settings.setLastOutputDirectory(properties.getProperty("lastOutputDirectory", settings.getLastOutputDirectory()));
            settings.setLastVideoDirectory(properties.getProperty("lastVideoDirectory", settings.getLastVideoDirectory()));
            settings.setVideoScaleFactor(Integer.parseInt(properties.getProperty("videoScaleFactor", Integer.toString(settings.getVideoScaleFactor()))));
            settings.setVideoTargetFrameRate(Integer.parseInt(properties.getProperty("videoTargetFrameRate", Integer.toString(settings.getVideoTargetFrameRate()))));
            settings.setVideoQualityPreset(properties.getProperty("videoQualityPreset", settings.getVideoQualityPreset()));
            settings.setVideoEncoderId(properties.getProperty("videoEncoderId", settings.getVideoEncoderId()));
            settings.setWindowWidth(Integer.parseInt(properties.getProperty("windowWidth", Integer.toString(settings.getWindowWidth()))));
            settings.setWindowHeight(Integer.parseInt(properties.getProperty("windowHeight", Integer.toString(settings.getWindowHeight()))));
        } catch (Exception ignored) {
            // Fall back to defaults if settings are partially corrupted.
        }

        settings.normalize();
        return settings;
    }

    public void save(AppSettings settings) throws IOException {
        settings.normalize();
        AppDirectories.ensureLayout();
        Path file = AppDirectories.settingsFile();

        Properties properties = new Properties();
        properties.setProperty("selectedModel", settings.getSelectedModel());
        properties.setProperty("scaleFactor", Integer.toString(settings.getScaleFactor()));
        properties.setProperty("deviceSelection", settings.getDeviceSelection());
        properties.setProperty("tileSize", Integer.toString(settings.getTileSize()));
        properties.setProperty("threadProfile", settings.getThreadProfile());
        properties.setProperty("ttaEnabled", Boolean.toString(settings.isTtaEnabled()));
        properties.setProperty("lastInputDirectory", settings.getLastInputDirectory());
        properties.setProperty("lastOutputDirectory", settings.getLastOutputDirectory());
        properties.setProperty("lastVideoDirectory", settings.getLastVideoDirectory());
        properties.setProperty("videoScaleFactor", Integer.toString(settings.getVideoScaleFactor()));
        properties.setProperty("videoTargetFrameRate", Integer.toString(settings.getVideoTargetFrameRate()));
        properties.setProperty("videoQualityPreset", settings.getVideoQualityPreset());
        properties.setProperty("videoEncoderId", settings.getVideoEncoderId());
        properties.setProperty("windowWidth", Integer.toString(settings.getWindowWidth()));
        properties.setProperty("windowHeight", Integer.toString(settings.getWindowHeight()));

        try (OutputStream output = Files.newOutputStream(file)) {
            properties.store(output, "Upscaler settings");
        }
    }
}
