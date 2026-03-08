package upscaler.service;

import upscaler.model.ScalePlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ScalePlanner {
    private static final List<Integer> SUPPORTED_TARGETS = List.of(2, 3, 4, 6, 8);
    private static final List<Integer> ENGINE_FACTORS = List.of(4, 3, 2);

    private ScalePlanner() {
    }

    public static List<Integer> supportedTargets() {
        return SUPPORTED_TARGETS;
    }

    public static ScalePlan plan(int targetScale) {
        List<Integer> passes = factorize(targetScale);
        if (passes.isEmpty()) {
            throw new IllegalArgumentException("Unsupported target scale: x" + targetScale);
        }
        return new ScalePlan(targetScale, Collections.unmodifiableList(passes));
    }

    private static List<Integer> factorize(int targetScale) {
        if (targetScale == 1) {
            return new ArrayList<>();
        }

        for (int factor : ENGINE_FACTORS) {
            if (targetScale == factor) {
                return new ArrayList<>(List.of(factor));
            }
            if (targetScale % factor == 0) {
                List<Integer> tail = factorize(targetScale / factor);
                if (!tail.isEmpty()) {
                    List<Integer> plan = new ArrayList<>();
                    plan.add(factor);
                    plan.addAll(tail);
                    return plan;
                }
            }
        }
        return List.of();
    }
}
