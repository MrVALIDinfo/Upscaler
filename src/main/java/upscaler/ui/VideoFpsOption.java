package upscaler.ui;

public record VideoFpsOption(int value, String label) {
    @Override
    public String toString() {
        return label;
    }
}
