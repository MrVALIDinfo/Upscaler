package ImageScaler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ImageUpscaler {

    private static Process currentProcess;

    public static void cancelRunningProcess() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            currentProcess = null;
            System.out.println("[RealESRGAN] Process was cancelled.");
        }
    }

    public static void upscaleImage(String inputImagePath, String outputImagePath, String modelName)
            throws IOException, InterruptedException {

        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("[RealESRGAN] Detected OS: " + os);

        String executablePath;

        if (os.contains("win")) {
            // Windows: используем .exe
            executablePath = "src/main/java/Models/AI/REALESRGAN/realesrgan-ncnn-vulkan.exe";
        } else if (os.contains("mac") || os.contains("linux") || os.contains("nux") || os.contains("nix")) {
            // macOS и Linux: бинарник без расширения
            executablePath = "src/main/java/Models/AI/REALESRGAN/realesrgan-ncnn-vulkan";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        File execFile = new File(executablePath);
        System.out.println("[RealESRGAN] Using binary: " + execFile.getAbsolutePath());

        if (!execFile.exists()) {
            throw new FileNotFoundException("Binary not found: " + execFile.getAbsolutePath());
        }

        if (!execFile.canExecute()) {
            boolean ok = execFile.setExecutable(true);
            System.out.println("[RealESRGAN] setExecutable(true) -> " + ok);
        }

        ProcessBuilder builder = new ProcessBuilder(
                execFile.getAbsolutePath(),
                "-i", inputImagePath,
                "-o", outputImagePath,
                "-n", modelName
                // Для x4 моделей scale по умолчанию 4x, параметр -s не обязателен
        );

        // Рабочая директория — там где лежит бинарник и папка models
        builder.directory(execFile.getParentFile());
        builder.redirectErrorStream(true);

        System.out.println("[RealESRGAN] Working dir: " + builder.directory().getAbsolutePath());
        System.out.println("[RealESRGAN] Command: " + String.join(" ", builder.command()));

        currentProcess = builder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(currentProcess.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RealESRGAN] " + line);
            }
        }

        int exitCode = currentProcess.waitFor();
        currentProcess = null;

        System.out.println("[RealESRGAN] Exit code: " + exitCode);

        if (exitCode != 0) {
            throw new RuntimeException("RealESRGAN exited with code: " + exitCode);
        }
    }
}
