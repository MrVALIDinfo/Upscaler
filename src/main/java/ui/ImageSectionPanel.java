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

    private static final int PREVIEW_W = 800;
    private static final int PREVIEW_H = 400;

    public ImageSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(40, 44, 52)); // FlatLaf немного темнее

        JLabel title = new JLabel("Upscale Image", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        previewScroll = new JScrollPane(imageLabel);
        previewScroll.setPreferredSize(new Dimension(PREVIEW_W, PREVIEW_H));
        previewScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65)));
        previewScroll.getViewport().setBackground(new Color(30, 33, 36));

        // Кнопки операций
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 14));
        buttons.setBackground(new Color(40, 44, 52));

        JButton upload = createMainButton("Open");
        JButton upscale = createMainButton("Upscale");
        JButton save = createMainButton("Save");
        JButton zoomIn = createMainButton("Zoom +");
        JButton zoomOut = createMainButton("Zoom -");

        buttons.add(upload);
        buttons.add(upscale);
        buttons.add(save);
        buttons.add(zoomIn);
        buttons.add(zoomOut);

        // Обработчики
        upload.addActionListener(e -> {
            BufferedImage img = InputImage.loadImage();
            if (img != null) {
                currentImage = img;
                zoom = getInitialZoom(img.getWidth(), img.getHeight());
                showPreview();
            } else {
                JOptionPane.showMessageDialog(this, "No file selected.");
            }
        });

        upscale.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Please load an image first.");
                return;
            }

            Panel4imageScaler dialog = new Panel4imageScaler((Frame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            String scaleOption = dialog.getSelectedScale();

            if (scaleOption == null) return;

            try {
                String basePath = "src/main/java/Models/AI/REALESRGAN/";
                File inputTemp = new File(basePath + "image_temp_input.png");
                File outputFile = new File(basePath + "image_temp_output.png");

                ImageIO.write(currentImage, "png", inputTemp);
                ImageUpscaler.upscaleImage(inputTemp.getAbsolutePath(), outputFile.getAbsolutePath());

                if (!outputFile.exists()) {
                    throw new IOException("Output file not found at: " + outputFile.getAbsolutePath());
                }

                BufferedImage resultImage = ImageIO.read(outputFile);
                currentImage = resultImage;
                zoom = getInitialZoom(resultImage.getWidth(), resultImage.getHeight());
                showPreview();

                JOptionPane.showMessageDialog(this, "✅ Upscaled x" + scaleOption);
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            }
        });

        save.addActionListener(e -> {
            if (currentImage != null) {
                OutputImage.saveImage(currentImage);
            } else {
                JOptionPane.showMessageDialog(this, "Nothing to save.");
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
                if (e.getWheelRotation() < 0)
                    zoom = Math.min(zoom * 1.1, 8.0);
                else
                    zoom = Math.max(zoom / 1.1, getInitialZoom(currentImage.getWidth(), currentImage.getHeight()));
                showPreview();
            }
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        center.add(previewScroll);
        center.add(Box.createVerticalStrut(10));
        center.add(buttons);

        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
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
        btn.setBackground(new Color(60, 63, 65));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}