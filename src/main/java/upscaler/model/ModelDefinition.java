package upscaler.model;

public record ModelDefinition(
        String name,
        String displayName,
        String description,
        ModelSource source
) {
    @Override
    public String toString() {
        return displayName;
    }
}
