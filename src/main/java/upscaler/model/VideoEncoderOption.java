package upscaler.model;

public record VideoEncoderOption(
        String id,
        String displayName,
        String description,
        boolean hardwareAccelerated
) {
    public boolean isAuto() {
        return "AUTO".equalsIgnoreCase(id);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
