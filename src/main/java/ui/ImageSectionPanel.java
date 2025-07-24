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
    private final JComboBox<String> modelBox;
    private final JProgressBar progressBar;

    private static final int PREVIEW_W = 800;
    private static final int PREVIEW_H = 400;

    public ImageSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(40, 44, 52));

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

        modelBox = new JComboBox<>(new String[]{
                "realesrgan-x4plus",
                "realesrgan-x4plus-anime",
                "realesr-animevideov3"
        });
        modelBox.setMaximumSize(new Dimension(200, 30));
        modelBox.setBackground(new Color(60, 63, 65));
        modelBox.setForeground(Color.WHITE);
        modelBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        modelBox.setFocusable(false);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setForeground(new Color(100, 200, 100));
        progressBar.setPreferredSize(new Dimension(200, 20));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topBar.setBackground(getBackground());
        topBar.add(modelBox);
        topBar.add(progressBar);

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

            final String model = (String) modelBox.getSelectedItem();

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Upscaling...");
            setButtonsEnabled(buttons, false);

            new SwingWorker<BufferedImage, Void>() {
                @Override
                protected BufferedImage doInBackground() throws Exception {
                    String path = "src/main/java/Models/AI/REALESRGAN/";
                    File inputTemp = new File(path + "image_temp_input.png");
                    File outputFile = new File(path + "image_temp_output.png");

                    ImageIO.write(currentImage, "png", inputTemp);
                    ImageUpscaler.upscaleImage(inputTemp.getAbsolutePath(), outputFile.getAbsolutePath(), model);

                    if (!outputFile.exists())
                        throw new IOException("Output file not found: " + outputFile.getAbsolutePath());

                    BufferedImage result = ImageIO.read(outputFile);
                    inputTemp.delete();
                    outputFile.delete();

                    return result;
                }

                @Override
                protected void done() {
                    try {
                        currentImage = get();
                        zoom = getInitialZoom(currentImage.getWidth(), currentImage.getHeight());
                        showPreview();
                        JOptionPane.showMessageDialog(ImageSectionPanel.this,
                                "✅ Upscale done with model: " + model);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ImageSectionPanel.this,
                                "❌ Upscale failed:\n" + ex.getMessage());
                    } finally {
                        progressBar.setVisible(false);
                        progressBar.setIndeterminate(false);
                        setButtonsEnabled(buttons, true);
                    }
                }
            }.execute();
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
                zoom = (e.getWheelRotation() < 0)
                        ? Math.min(zoom * 1.1, 8.0)
                        : Math.max(zoom / 1.1, getInitialZoom(currentImage.getWidth(), currentImage.getHeight()));
                showPreview();
            }
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        center.add(title);
        center.add(Box.createVerticalStrut(10));
        center.add(previewScroll);

        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);
        page.add(center, BorderLayout.CENTER);
        page.add(buttons, BorderLayout.SOUTH);

        add(topBar, BorderLayout.NORTH);
        add(page, BorderLayout.CENTER);
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

    private void setButtonsEnabled(JPanel panel, boolean enabled) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) comp.setEnabled(enabled);
        }
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
        return Math.min(scale, 1.0);
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