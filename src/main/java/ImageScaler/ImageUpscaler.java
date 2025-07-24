package ImageScaler;

import java.io.*;

public class ImageUpscaler {

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

        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RealESRGAN] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0)
            throw new RuntimeException("RealESRGAN exited with code: " + exitCode);
    }
}