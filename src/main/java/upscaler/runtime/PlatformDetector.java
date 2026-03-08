package upscaler.runtime;

public final class PlatformDetector {
    private PlatformDetector() {
    }

    public static Platform detect() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();

        if (os.contains("win") && (arch.contains("64") || arch.contains("amd64") || arch.contains("x86_64"))) {
            return Platform.WINDOWS_X64;
        }
        if (os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64") || arch.contains("x86_64") || arch.contains("amd64"))) {
            return Platform.MACOS_UNIVERSAL;
        }
        if ((os.contains("linux") || os.contains("nux")) && (arch.contains("64") || arch.contains("amd64") || arch.contains("x86_64"))) {
            return Platform.LINUX_X64;
        }

        throw new IllegalStateException("Unsupported OS/architecture: " + os + " / " + arch);
    }
}
