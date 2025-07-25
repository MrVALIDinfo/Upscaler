package ImageScaler;

import java.io.*;

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
        String executablePath;

        if (os.contains("win")) {
            executablePath = "src/main/java/Models/AI/REALESRGAN/realesrgan-ncnn-vulkan.exe";
        } else if (os.contains("mac")) {
            executablePath = "src/main/java/Models/AI/REALESRGAN/realesrgan-ncnn-vulkan";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        File execFile = new File(executablePath);
        if (!execFile.exists()) throw new FileNotFoundException("Binary not found: " + execFile);
        if (!execFile.canExecute()) execFile.setExecutable(true);

        ProcessBuilder builder = new ProcessBuilder(
                execFile.getAbsolutePath(),
                "-i", inputImagePath,
                "-o", outputImagePath,
                "-n", modelName
        );

        builder.directory(new File("src/main/java/Models/AI/REALESRGAN"));
        builder.redirectErrorStream(true);

        currentProcess = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RealESRGAN] " + line);
            }
        }

        int exitCode = currentProcess.waitFor();
        currentProcess = null;

        if (exitCode != 0) {
            throw new RuntimeException("RealESRGAN exited with code: " + exitCode);
        }
    }
}