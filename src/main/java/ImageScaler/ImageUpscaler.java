package ImageScaler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;

public class ImageUpscaler {

    public static void upscaleImage(String inputImagePath, String outputImagePath) throws IOException, InterruptedException {
        // Путь к .exe
        String executablePath = "C:\\Users\\acer\\IdeaProjects\\Upscaler\\src\\main\\java\\Models\\AI\\REALESRGAN\\realesrgan-ncnn-vulkan.exe";

        // Формируем правильную команду, разбив на части
        ProcessBuilder builder = new ProcessBuilder(
                executablePath,
                "-i", inputImagePath,
                "-o", outputImagePath,
                "-n", "realesrgan-x4plus" // безопасная универсальная модель
        );

        // Указываем рабочую директорию процесса (там .exe и .dll)
        builder.directory(new File("C:\\Users\\acer\\IdeaProjects\\Upscaler\\src\\main\\java\\Models\\AI\\REALESRGAN"));

        // Объединяем stdout + stderr в один поток
        builder.redirectErrorStream(true);

        Process process = builder.start();

        // Читаем и выводим логи процесса
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("RealESRGAN process exited with code " + exitCode);
        }

        System.out.println("🎉 Готово! Изображение успешно апскейлено.");
    }

    public static void main(String[] args) {
        try {
            // 🔁 Замени пути на реальные
            upscaleImage("C:\\Users\\acer\\Pictures\\test.jpg", "C:\\Users\\acer\\Pictures\\test_upscaled.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}