package upscaler.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScalePlannerTest {
    @Test
    void plansSinglePassScalesExactly() {
        assertEquals("4x ai -> 2x output (0.50x resize)", ScalePlanner.plan(2).describe());
        assertEquals("4x ai -> 3x output (0.75x resize)", ScalePlanner.plan(3).describe());
        assertEquals("4x ai", ScalePlanner.plan(4).describe());
    }

    @Test
    void plansSafeOutscaleBeyondNativeResolution() {
        assertEquals("4x ai -> 6x output (1.50x resize)", ScalePlanner.plan(6).describe());
        assertEquals("4x ai -> 8x output (2.00x resize)", ScalePlanner.plan(8).describe());
    }

    @Test
    void rejectsUnsupportedScales() {
        assertThrows(IllegalArgumentException.class, () -> ScalePlanner.plan(5));
        assertThrows(IllegalArgumentException.class, () -> ScalePlanner.plan(7));
    }

    @Test
    void supportsIdentityPlanForVideoPassthrough() {
        assertEquals("1x passthrough", ScalePlanner.plan(1).describe());
    }
}
