package ui;

import ImageScaler.ImageUpscaler;
import ImageScaler.InputImage;
import ImageScaler.OutputImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageSectionPanel extends JPanel {

    private BufferedImage currentImage;
    private JLabel imageLabel = new JLabel();
    private JScrollPane previewScroll;
    private double zoom = 1.0;

    private static final int PREVIEW_W = 840;
    private static final int PREVIEW_H = 360;

    public ImageSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(247, 248, 250));

        JLabel title = new JLabel("Image Upscaling", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        previewScroll = new JScrollPane(imageLabel);
        previewScroll.setPreferredSize(new Dimension(PREVIEW_W, PREVIEW_H));
        previewScroll.getViewport().setBackground(Color.LIGHT_GRAY);
        previewScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 16));
        JButton upload = createMainButton("Upload");
        JButton upscale = createMainButton("Upscale");
        JButton download = createMainButton("Download");
        JButton zoomIn = createMainButton("+");
        JButton zoomOut = createMainButton("-");

        buttons.add(upload);
        buttons.add(upscale);
        buttons.add(download);
        buttons.add(zoomIn);
        buttons.add(zoomOut);

        upload.addActionListener(e -> {
            BufferedImage img = InputImage.loadImage();
            if (img != null) {
                currentImage = img;
                zoom = getInitialZoom(img.getWidth(), img.getHeight());
                showPreview();
            } else {
                JOptionPane.showMessageDialog(this, "Файл не выбран.");
            }
        });

        upscale.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Сначала загрузите изображение.");
                return;
            }

            Panel4imageScaler dialog = new Panel4imageScaler((Frame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            String scaleOption = dialog.getSelectedScale();

            if (scaleOption == null) {
                return;
            }

            try {
                // Путь в ту же папку, где и RealESRGAN.exe
                String basePath = "src/main/java/Models/AI/REALESRGAN/";
                File inputTemp = new File(basePath + "image_temp_input.png");
                File outputFile = new File(basePath + "image_temp_output.png");

                // Сохраняем картинку во временный файл
                ImageIO.write(currentImage, "png", inputTemp);

                // Апскейлим
                ImageUpscaler.upscaleImage(inputTemp.getAbsolutePath(), outputFile.getAbsolutePath());

                // Проверка перед чтением
                System.out.println("[DEBUG] Output path: " + outputFile.getAbsolutePath());
                System.out.println("[DEBUG] Exists: " + outputFile.exists());
                System.out.println("[DEBUG] Size: " + outputFile.length() + " bytes");

                if (!outputFile.exists()) {
                    throw new IOException("Файл upscale-результата не найден: " + outputFile.getAbsolutePath());
                }

                // Читаем upscale изображение
                BufferedImage resultImage = ImageIO.read(outputFile);

                currentImage = resultImage;
                zoom = getInitialZoom(resultImage.getWidth(), resultImage.getHeight());
                showPreview();

                JOptionPane.showMessageDialog(this, "✅ Апскейлинг завершен! (" + scaleOption + ")");

            } catch (IOException | InterruptedException ex) {
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

        zoomIn.addActionListener(e -> {
            if (currentImage != null) {
                zoom = Math.min(zoom * 1.25, 8.0);
                showPreview();
            }
        });

        zoomOut.addActionListener(e -> {
            if (currentImage != null) {
                zoom = Math.max(zoom / 1.25, getInitialZoom(currentImage.getWidth(), currentImage.getHeight()));
                showPreview();
            }
        });

        previewScroll.addMouseWheelListener(e -> {
            if (currentImage != null && e.isControlDown()) {
                if (e.getWheelRotation() < 0) {
                    zoom = Math.min(zoom * 1.1, 8.0);
                } else {
                    zoom = Math.max(zoom / 1.1, getInitialZoom(currentImage.getWidth(), currentImage.getHeight()));
                }
                showPreview();
            }
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(previewScroll);
        center.add(Box.createVerticalStrut(10));

        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void showPreview() {
        if (currentImage == null) return;
        int w = (int) (currentImage.getWidth() * zoom);
        int h = (int) (currentImage.getHeight() * zoom);
        Image scaled = getHighQualityScaledImage(currentImage, w, h);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.setPreferredSize(new Dimension(w, h));
        imageLabel.revalidate();
    }

    private Image getHighQualityScaledImage(BufferedImage src, int w, int h) {
        if (w <= 0 || h <= 0) return src;
        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return resized;
    }

    private double getInitialZoom(int imgW, int imgH) {
        double maxW = PREVIEW_W, maxH = PREVIEW_H;
        double scale = Math.min(maxW / imgW, maxH / imgH);
        return scale < 1.0 ? scale : 1.0;
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