package ImageScaler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class OutputImage {
    public static void saveImage(BufferedImage img) {
        if (img == null) return;
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".png")) f = new File(f + ".png");
            try {
                ImageIO.write(img, "png", f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
