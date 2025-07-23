package ImageScaler;
import ai.onnxruntime.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class ImageUpscaler {

    public static void upscale(String inputImagePath) throws Exception {
        String modelPath = "src/main/java/ModelsAI/super-resolution-10.onnx";

        // Загружаем изображение
        BufferedImage img = ImageIO.read(new File(inputImagePath));
        int width = img.getWidth();
        int height = img.getHeight();

        // Преобразуем в grayscale / Y канал
        float[][][][] inputTensor = new float[1][1][height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                float yChannel = (float) (0.299 * r + 0.587 * g + 0.114 * b) / 255f;
                inputTensor[0][0][y][x] = yChannel;
            }
        }

        // ONNX Runtime
        try (OrtEnvironment env = OrtEnvironment.getEnvironment();
             OrtSession session = env.createSession(modelPath, new OrtSession.SessionOptions())) {

            // Создаём Tensor
            OnnxTensor input = OnnxTensor.createTensor(env, inputTensor);
            String inputName = session.getInputNames().iterator().next();

            // Запускаем модель
            OrtSession.Result result = session.run(Collections.singletonMap(inputName, input));
            float[][][][] output = (float[][][][]) result.get(0).getValue();

            // Извлекаем размеры
            int outH = output[0][0].length;
            int outW = output[0][0][0].length;

            // Результат в изображение (grayscale)
            BufferedImage outImage = new BufferedImage(outW, outH, BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 0; y < outH; y++) {
                for (int x = 0; x < outW; x++) {
                    float pixel = Math.min(Math.max(output[0][0][y][x], 0.0f), 1.0f);
                    int gray = (int)(pixel * 255);
                    int rgb = (gray << 16) | (gray << 8) | gray;
                    outImage.setRGB(x, y, rgb);
                }
            }

            // Сохраняем результат
            String outputPath = inputImagePath.replace(".jpg", "_upscaled.png").replace(".png", "_upscaled.png");
            ImageIO.write(outImage, "png", new File(outputPath));
            System.out.println("✅ Апскейлинг завершен! Файл: " + outputPath);
        }
    }
}