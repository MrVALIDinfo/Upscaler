package upscaler.app;

import upscaler.config.AppDirectories;
import upscaler.config.AppSettings;
import upscaler.config.SettingsRepository;
import upscaler.runtime.BundledRuntimeManager;
import upscaler.service.DeviceDetectionService;
import upscaler.service.ModelRegistry;
import upscaler.service.RealEsrganService;
import upscaler.service.VideoMetadataService;
import upscaler.service.VideoProcessingService;
import upscaler.service.VideoToolingService;
import upscaler.ui.ApplicationTheme;
import upscaler.ui.MainFrame;

import javax.swing.SwingUtilities;

public final class UpscalerApplication {
    public static final String APP_VERSION = "1.0.0";

    private UpscalerApplication() {
    }

    public static void launch(String[] args) {
        try {
            AppDirectories.ensureLayout();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize application directories", e);
        }

        SettingsRepository settingsRepository = new SettingsRepository();
        AppSettings settings = settingsRepository.load();
        BundledRuntimeManager runtimeManager = new BundledRuntimeManager(APP_VERSION);
        ModelRegistry modelRegistry = new ModelRegistry();
        RealEsrganService upscaleService = new RealEsrganService(runtimeManager, modelRegistry);
        DeviceDetectionService deviceDetectionService = new DeviceDetectionService(runtimeManager);
        VideoToolingService videoToolingService = new VideoToolingService();
        VideoMetadataService videoMetadataService = new VideoMetadataService(videoToolingService);
        VideoProcessingService videoProcessingService = new VideoProcessingService(videoMetadataService, videoToolingService, upscaleService);

        ApplicationTheme.install();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(
                    settings,
                    settingsRepository,
                    modelRegistry,
                    deviceDetectionService,
                    upscaleService,
                    videoToolingService,
                    videoMetadataService,
                    videoProcessingService
            );
            frame.setVisible(true);
        });
    }
}
