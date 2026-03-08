package upscaler.config;

import java.nio.file.Path;

public class AppSettings {
    public static final String AUTO_DEVICE = "AUTO";

    private String selectedModel = "realesrgan-x4plus";
    private int scaleFactor = 4;
    private String deviceSelection = AUTO_DEVICE;
    private int tileSize = 0;
    private String threadProfile = "1:2:2";
    private boolean ttaEnabled;
    private String lastInputDirectory = "";
    private String lastOutputDirectory = "";
    private String lastVideoDirectory = "";
    private int videoScaleFactor = 2;
    private int videoTargetFrameRate = 60;
    private String videoQualityPreset = "BALANCED";
    private String videoEncoderId = "AUTO";
    private int windowWidth = 1440;
    private int windowHeight = 920;

    public AppSettings copy() {
        AppSettings copy = new AppSettings();
        copy.selectedModel = selectedModel;
        copy.scaleFactor = scaleFactor;
        copy.deviceSelection = deviceSelection;
        copy.tileSize = tileSize;
        copy.threadProfile = threadProfile;
        copy.ttaEnabled = ttaEnabled;
        copy.lastInputDirectory = lastInputDirectory;
        copy.lastOutputDirectory = lastOutputDirectory;
        copy.lastVideoDirectory = lastVideoDirectory;
        copy.videoScaleFactor = videoScaleFactor;
        copy.videoTargetFrameRate = videoTargetFrameRate;
        copy.videoQualityPreset = videoQualityPreset;
        copy.videoEncoderId = videoEncoderId;
        copy.windowWidth = windowWidth;
        copy.windowHeight = windowHeight;
        return copy;
    }

    public void normalize() {
        if (selectedModel == null || selectedModel.isBlank()) {
            selectedModel = "realesrgan-x4plus";
        }
        if (scaleFactor != 2 && scaleFactor != 3 && scaleFactor != 4 && scaleFactor != 6 && scaleFactor != 8) {
            scaleFactor = 4;
        }
        if (videoScaleFactor != 1 && videoScaleFactor != 2 && videoScaleFactor != 3 && videoScaleFactor != 4 && videoScaleFactor != 6 && videoScaleFactor != 8) {
            videoScaleFactor = 2;
        }
        if (videoTargetFrameRate != 0 && videoTargetFrameRate != 60 && videoTargetFrameRate != 120 && videoTargetFrameRate != 240) {
            videoTargetFrameRate = 60;
        }
        if (videoQualityPreset == null || videoQualityPreset.isBlank()) {
            videoQualityPreset = "BALANCED";
        }
        if (videoEncoderId == null || videoEncoderId.isBlank()) {
            videoEncoderId = "AUTO";
        }
        if (deviceSelection == null || deviceSelection.isBlank()) {
            deviceSelection = AUTO_DEVICE;
        }
        if (tileSize < 0) {
            tileSize = 0;
        }
        if (threadProfile == null || threadProfile.isBlank()) {
            threadProfile = "1:2:2";
        }
        if (windowWidth < 1180) {
            windowWidth = 1180;
        }
        if (windowHeight < 760) {
            windowHeight = 760;
        }
        if (lastInputDirectory == null) {
            lastInputDirectory = "";
        }
        if (lastOutputDirectory == null) {
            lastOutputDirectory = "";
        }
        if (lastVideoDirectory == null) {
            lastVideoDirectory = "";
        }
    }

    public Path lastInputDirectoryPath() {
        return lastInputDirectory.isBlank() ? null : Path.of(lastInputDirectory);
    }

    public Path lastOutputDirectoryPath() {
        return lastOutputDirectory.isBlank() ? null : Path.of(lastOutputDirectory);
    }

    public Path lastVideoDirectoryPath() {
        return lastVideoDirectory.isBlank() ? null : Path.of(lastVideoDirectory);
    }

    public String getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public String getDeviceSelection() {
        return deviceSelection;
    }

    public void setDeviceSelection(String deviceSelection) {
        this.deviceSelection = deviceSelection;
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public String getThreadProfile() {
        return threadProfile;
    }

    public void setThreadProfile(String threadProfile) {
        this.threadProfile = threadProfile;
    }

    public boolean isTtaEnabled() {
        return ttaEnabled;
    }

    public void setTtaEnabled(boolean ttaEnabled) {
        this.ttaEnabled = ttaEnabled;
    }

    public String getLastInputDirectory() {
        return lastInputDirectory;
    }

    public void setLastInputDirectory(String lastInputDirectory) {
        this.lastInputDirectory = lastInputDirectory;
    }

    public String getLastOutputDirectory() {
        return lastOutputDirectory;
    }

    public void setLastOutputDirectory(String lastOutputDirectory) {
        this.lastOutputDirectory = lastOutputDirectory;
    }

    public String getLastVideoDirectory() {
        return lastVideoDirectory;
    }

    public void setLastVideoDirectory(String lastVideoDirectory) {
        this.lastVideoDirectory = lastVideoDirectory;
    }

    public int getVideoScaleFactor() {
        return videoScaleFactor;
    }

    public void setVideoScaleFactor(int videoScaleFactor) {
        this.videoScaleFactor = videoScaleFactor;
    }

    public int getVideoTargetFrameRate() {
        return videoTargetFrameRate;
    }

    public void setVideoTargetFrameRate(int videoTargetFrameRate) {
        this.videoTargetFrameRate = videoTargetFrameRate;
    }

    public String getVideoQualityPreset() {
        return videoQualityPreset;
    }

    public void setVideoQualityPreset(String videoQualityPreset) {
        this.videoQualityPreset = videoQualityPreset;
    }

    public String getVideoEncoderId() {
        return videoEncoderId;
    }

    public void setVideoEncoderId(String videoEncoderId) {
        this.videoEncoderId = videoEncoderId;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }
}
