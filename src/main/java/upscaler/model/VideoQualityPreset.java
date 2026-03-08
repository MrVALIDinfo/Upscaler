package upscaler.model;

public enum VideoQualityPreset {
    FAST("Fast", "fast", 22),
    BALANCED("Balanced", "medium", 18),
    QUALITY("Quality", "slow", 16),
    MAXIMUM("Maximum", "slower", 14);

    private final String displayName;
    private final String x264Preset;
    private final int crf;

    VideoQualityPreset(String displayName, String x264Preset, int crf) {
        this.displayName = displayName;
        this.x264Preset = x264Preset;
        this.crf = crf;
    }

    public String displayName() {
        return displayName;
    }

    public String x264Preset() {
        return x264Preset;
    }

    public int crf() {
        return crf;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
