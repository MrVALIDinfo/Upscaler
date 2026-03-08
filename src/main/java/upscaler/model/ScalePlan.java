package upscaler.model;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public record ScalePlan(int targetScale, int nativeScale, List<Integer> passes, double postResizeFactor) {
    public int engineScale() {
        return passes.stream().reduce(1, (left, right) -> left * right);
    }

    public boolean requiresPostResize() {
        return Math.abs(postResizeFactor - 1d) > 0.0001d;
    }

    public String describe() {
        if (passes.isEmpty()) {
            return "1x passthrough";
        }

        String engineText = passes.stream()
                .map(step -> step + "x")
                .collect(Collectors.joining(" + "));
        if (!requiresPostResize()) {
            return engineText + " ai";
        }
        return engineText + " ai -> " + targetScale + "x output (" + String.format(Locale.US, "%.2f", postResizeFactor) + "x resize)";
    }
}
