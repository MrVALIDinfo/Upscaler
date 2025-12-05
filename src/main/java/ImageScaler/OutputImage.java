package ImageScaler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OutputImage {

    public static void saveImage(BufferedImage image) {
        try {
            JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home") + "/Pictures"));
            fileChooser.setDialogTitle("Сохранить изображение как...");
            fileChooser.setSelectedFile(new File("upscaled_image.png"));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".png")) {
                    filePath += ".png";
                    fileToSave = new File(filePath);
                }

                File parent = fileToSave.getCanonicalFile().getParentFile();
                if (parent == null || !parent.exists() || !parent.canWrite()) {
                    throw new IOException("❌ Папка недоступна для записи: " + parent);
                }

                ImageIO.write(image, "png", fileToSave);
                JOptionPane.showMessageDialog(null,
                        "✅ Изображение успешно сохранено:\n" + fileToSave.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "❌ Не удалось сохранить файл:\n" + e.getMessage(),
                    "Ошибка сохранения", JOptionPane.ERROR_MESSAGE);
        }
    }
}
