package ImageScaler;

import java.io.*;

public class ImageUpscaler {

    public static void upscaleImage(String inputImagePath, String outputImagePath) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        String executablePath;

        if (os.contains("win")) {
            executablePath = "src/main/java/Models/AI/REALESRGAN/realesrgan-ncnn-vulkan.exe";
        } else if (os.contains("mac")) {
            executablePath = "src/main/java/Models/AI/REALESRGAN/realesrgan-ncnn-vulkan";
        } else {
            throw new UnsupportedOperationException("❌ Неподдерживаемая ОС: " + os);
        }

        File execFile = new File(executablePath);
        if (!execFile.exists()) {
            throw new FileNotFoundException("❌ Не найден файл модели (бинарник): " + executablePath);
        }

        if (!execFile.canExecute()) {
            System.out.println("⚠️ Выдаю права на запуск: " + execFile.getAbsolutePath());
            execFile.setExecutable(true); // даст права на выполнение, если возможно
        }

        ProcessBuilder builder = new ProcessBuilder(
                execFile.getAbsolutePath(),
                "-i", inputImagePath,
                "-o", outputImagePath,
                "-n", "realesrgan-x4plus"
        );

        // рабочая директория (там модели, .dll и т.п.)
        builder.directory(new File("src/main/java/Models/AI/REALESRGAN"));
        builder.redirectErrorStream(true);

        System.out.println("▶️ Запуск: " + String.join(" ", builder.command()));

        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RealESRGAN] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("❌ Процесс RealESRGAN завершился с кодом: " + exitCode);
        }

        System.out.println("✅ Апскейлинг завершён успешно.");
    }
}