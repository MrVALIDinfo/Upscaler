package upscaler.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppSettingsTest {
    @Test
    void normalizesInvalidVideoAndWindowSettings() {
        AppSettings settings = new AppSettings();
        settings.setScaleFactor(5);
        settings.setVideoScaleFactor(9);
        settings.setVideoTargetFrameRate(144);
        settings.setVideoQualityPreset("");
        settings.setVideoEncoderId("");
        settings.setWindowWidth(400);
        settings.setWindowHeight(300);

        settings.normalize();

        assertEquals(4, settings.getScaleFactor());
        assertEquals(2, settings.getVideoScaleFactor());
        assertEquals(60, settings.getVideoTargetFrameRate());
        assertEquals("BALANCED", settings.getVideoQualityPreset());
        assertEquals("AUTO", settings.getVideoEncoderId());
        assertEquals(1180, settings.getWindowWidth());
        assertEquals(760, settings.getWindowHeight());
    }
}
