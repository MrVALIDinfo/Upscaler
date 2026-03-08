package upscaler.runtime;

import java.util.List;

public enum Platform {
    WINDOWS_X64("windows-x86_64", "realesrgan-ncnn-vulkan.exe", List.of("vcomp140.dll", "vcomp140d.dll")),
    LINUX_X64("linux-x86_64", "realesrgan-ncnn-vulkan", List.of()),
    MACOS_UNIVERSAL("macos-universal", "realesrgan-ncnn-vulkan", List.of());

    private final String resourceFolder;
    private final String executableName;
    private final List<String> sidecarFiles;

    Platform(String resourceFolder, String executableName, List<String> sidecarFiles) {
        this.resourceFolder = resourceFolder;
        this.executableName = executableName;
        this.sidecarFiles = sidecarFiles;
    }

    public String resourceFolder() {
        return resourceFolder;
    }

    public String executableName() {
        return executableName;
    }

    public List<String> sidecarFiles() {
        return sidecarFiles;
    }
}
