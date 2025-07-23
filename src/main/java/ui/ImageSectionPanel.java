package ui;
import ImageScaler.OutputImage;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageSectionPanel extends JPanel {

    private BufferedImage currentImage; // поле для сохранения загруженного/обработанного изображения

    public ImageSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(247, 248, 250));

        JLabel title = new JLabel("Image Upscaling", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel preview = new JPanel();
        preview.setPreferredSize(new Dimension(420, 180));
        preview.setBackground(Color.LIGHT_GRAY);
        preview.add(new JLabel("Image Preview"));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 16));
        JButton upload = createMainButton("Upload");
        JButton upscale = createMainButton("Upscale");
        JButton download = createMainButton("Download");

        buttons.add(upload);
        buttons.add(upscale);
        buttons.add(download);

        upload.addActionListener(e -> {
            BufferedImage img = ImageScaler.InputImage.loadImage();
            if (img != null) {
                currentImage = img; // сохраняем загруженное изображение
                JOptionPane.showMessageDialog(this, "Изображение загружено: " + img.getWidth() + "x" + img.getHeight());
            } else {
                JOptionPane.showMessageDialog(this, "Файл не выбран.");
            }
        });

        upscale.addActionListener(e -> JOptionPane.showMessageDialog(this, "Upscale clicked"));

        download.addActionListener(e -> {
            if (currentImage != null) {
                OutputImage.saveImage(currentImage); // вызываем метод сохранения
            } else {
                JOptionPane.showMessageDialog(this, "Нет изображения для сохранения.");
            }
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(preview);
        center.add(Box.createVerticalStrut(20));
        center.add(buttons);

        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private JButton createMainButton(String text) {
        JButton btn = new JButton(text.toUpperCase());
        btn.setBackground(new Color(35, 39, 47));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Dialog", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }
}
