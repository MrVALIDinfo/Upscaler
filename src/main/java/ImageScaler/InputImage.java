package ImageScaler;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InputImage {
    public static BufferedImage loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите изображение");

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                return ImageIO.read(file); // Загружаем изображение
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
