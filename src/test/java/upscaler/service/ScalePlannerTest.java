package upscaler.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScalePlannerTest {
    @Test
    void plansSinglePassScalesExactly() {
        assertEquals("2x", ScalePlanner.plan(2).describe());
        assertEquals("3x", ScalePlanner.plan(3).describe());
        assertEquals("4x", ScalePlanner.plan(4).describe());
    }

    @Test
    void plansMultiPassScalesExactly() {
        assertEquals("3x + 2x", ScalePlanner.plan(6).describe());
        assertEquals("4x + 2x", ScalePlanner.plan(8).describe());
    }

    @Test
    void rejectsUnsupportedScales() {
        assertThrows(IllegalArgumentException.class, () -> ScalePlanner.plan(5));
        assertThrows(IllegalArgumentException.class, () -> ScalePlanner.plan(7));
    }
}
