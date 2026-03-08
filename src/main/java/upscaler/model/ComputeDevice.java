package upscaler.model;

public record ComputeDevice(
        String id,
        String displayName,
        String description,
        String engineGpuId,
        boolean selectable,
        boolean softwareFallback
) {
    public boolean isAuto() {
        return "AUTO".equals(id);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
