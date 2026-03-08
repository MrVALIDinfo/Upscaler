package upscaler.runtime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatformDetectorTest {
    private final String originalOsName = System.getProperty("os.name");
    private final String originalOsArch = System.getProperty("os.arch");

    @AfterEach
    void restoreSystemProperties() {
        System.setProperty("os.name", originalOsName);
        System.setProperty("os.arch", originalOsArch);
    }

    @Test
    void detectsSupportedPlatforms() {
        System.setProperty("os.name", "Windows 11");
        System.setProperty("os.arch", "amd64");
        assertEquals(Platform.WINDOWS_X64, PlatformDetector.detect());

        System.setProperty("os.name", "Linux");
        System.setProperty("os.arch", "x86_64");
        assertEquals(Platform.LINUX_X64, PlatformDetector.detect());

        System.setProperty("os.name", "Mac OS X");
        System.setProperty("os.arch", "arm64");
        assertEquals(Platform.MACOS_UNIVERSAL, PlatformDetector.detect());
    }

    @Test
    void rejectsUnsupportedPlatforms() {
        System.setProperty("os.name", "Solaris");
        System.setProperty("os.arch", "sparc");
        assertThrows(IllegalStateException.class, PlatformDetector::detect);
    }
}
