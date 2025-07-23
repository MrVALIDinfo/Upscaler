package ui;

import  ImageScaler.InputImage;
import ImageScaler.OutputImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import ImageScaler.ImageUpscaler;
public class ImageSectionPanel extends JPanel {

    private BufferedImage currentImage;
    private JLabel imageLabel = new JLabel();
    private JPanel preview;

    public ImageSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(247, 248, 250));

        JLabel title = new JLabel("Image Upscaling", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        preview = new JPanel();
        preview.setPreferredSize(new Dimension(420, 180));
        preview.setBackground(Color.LIGHT_GRAY);
        preview.setLayout(new BorderLayout());
        preview.add(imageLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 16));
        JButton upload = createMainButton("Upload");
        JButton upscale = createMainButton("Upscale");
        JButton download = createMainButton("Download");

        buttons.add(upload);
        buttons.add(upscale);
        buttons.add(download);

        upload.addActionListener(e -> {
            BufferedImage img = InputImage.loadImage();
            if (img != null) {
                currentImage = img;

                // Получаем текущие размеры панели
                int panelWidth = preview.getWidth();
                int panelHeight = preview.getHeight();

                // Если ещё не отрисовано, берём preferredSize
                if (panelWidth == 0 || panelHeight == 0) {
                    panelWidth = preview.getPreferredSize().width;
                    panelHeight = preview.getPreferredSize().height;
                }

                // Масштабируем изображение пропорционально
                double scale = Math.min(
                        (double) panelWidth / img.getWidth(),
                        (double) panelHeight / img.getHeight()
                );
                int newW = (int) (img.getWidth() * scale);
                int newH = (int) (img.getHeight() * scale);

                Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setVerticalAlignment(SwingConstants.CENTER);

                revalidate();
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Файл не выбран.");
            }
        });

        upscale.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Сначала загрузите изображение.");
                return;
            }

            try {
                // Сохраняем текущее изображение во временный файл
                File inputTemp = new File("image_temp_input.png");
                ImageIO.write(currentImage, "png", inputTemp);

                // Запускаем апскейлинг
                ImageUpscaler.upscale(inputTemp.getAbsolutePath());

                // Загружаем результат (модель создаёт файл _upscaled.png)
                File outputFile = new File("image_temp_input_upscaled.png");
                BufferedImage resultImage = ImageIO.read(outputFile);

                // Обновляем текущую картинку
                currentImage = resultImage;

                // Отображаем в интерфейсе
                int panelWidth = preview.getWidth();
                int panelHeight = preview.getHeight();

                if (panelWidth == 0 || panelHeight == 0) {
                    panelWidth = preview.getPreferredSize().width;
                    panelHeight = preview.getPreferredSize().height;
                }

                double scale = Math.min(
                        (double) panelWidth / resultImage.getWidth(),
                        (double) panelHeight / resultImage.getHeight()
                );
                int newW = (int) (resultImage.getWidth() * scale);
                int newH = (int) (resultImage.getHeight() * scale);

                Image scaled = resultImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));

                revalidate();
                repaint();

                JOptionPane.showMessageDialog(this, "✅ Апскейлинг завершен!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Ошибка при апскейлинге: " + ex.getMessage());
            }
        });
        download.addActionListener(e -> {
            if (currentImage != null) {
                OutputImage.saveImage(currentImage);
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
