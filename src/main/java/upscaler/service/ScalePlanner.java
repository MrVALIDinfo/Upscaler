package upscaler.service;

import upscaler.model.ModelDefinition;
import upscaler.model.ScalePlan;

import java.util.List;

public final class ScalePlanner {
    private static final List<Integer> SUPPORTED_TARGETS = List.of(2, 3, 4, 6, 8);
    private static final int DEFAULT_NATIVE_SCALE = 4;

    private ScalePlanner() {
    }

    public static List<Integer> supportedTargets() {
        return SUPPORTED_TARGETS;
    }

    public static ScalePlan plan(int targetScale) {
        return plan(targetScale, DEFAULT_NATIVE_SCALE);
    }

    public static ScalePlan plan(ModelDefinition model, int targetScale) {
        return plan(targetScale, model == null ? DEFAULT_NATIVE_SCALE : model.nativeScale());
    }

    public static ScalePlan plan(int targetScale, int nativeScale) {
        if (targetScale == 1) {
            return new ScalePlan(1, nativeScale, List.of(), 1d);
        }
        if (!SUPPORTED_TARGETS.contains(targetScale)) {
            throw new IllegalArgumentException("Unsupported target scale: x" + targetScale);
        }

        int safeNativeScale = nativeScale > 0 ? nativeScale : DEFAULT_NATIVE_SCALE;
        return new ScalePlan(
                targetScale,
                safeNativeScale,
                List.of(safeNativeScale),
                targetScale / (double) safeNativeScale
        );
    }
}
