package upscaler.model;

import java.util.List;
import java.util.stream.Collectors;

public record ScalePlan(int targetScale, List<Integer> passes) {
    public String describe() {
        return passes.stream()
                .map(step -> step + "x")
                .collect(Collectors.joining(" + "));
    }
}
