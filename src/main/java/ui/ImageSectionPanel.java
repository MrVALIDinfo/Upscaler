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
    private final JLabel imageLabel = new JLabel("No image loaded", SwingConstants.CENTER);
    private double zoom = 1.0;
    private final JProgressBar progressBar;
    private SwingWorker<BufferedImage, Void> upscaleWorker;

    private static final int PREVIEW_W = 800;
    private static final int PREVIEW_H = 600;

    public ImageSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(40, 44, 52));

        JLabel title = new JLabel("Upscale Image", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setForeground(Color.GRAY);

        JScrollPane previewScroll = new JScrollPane(imageLabel);
        previewScroll.setPreferredSize(new Dimension(PREVIEW_W, PREVIEW_H));
        previewScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65)));
        previewScroll.getViewport().setBackground(new Color(30, 33, 36));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setForeground(new Color(100, 200, 100));
        progressBar.setPreferredSize(new Dimension(500, 22));
        progressBar.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        progressPanel.setBackground(getBackground());
        progressPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
        progressPanel.add(progressBar);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 14));
        buttons.setBackground(getBackground());

        JButton upload = createMainButton("Open");
        JButton upscale = createMainButton("Upscale");
        JButton clear = createMainButton("Clear");
        JButton save = createMainButton("Save");
        JButton zoomIn = createMainButton("Zoom +");
        JButton zoomOut = createMainButton("Zoom -");

        buttons.add(upload);
        buttons.add(upscale);
        buttons.add(clear);
        buttons.add(save);
        buttons.add(zoomIn);
        buttons.add(zoomOut);

        upload.addActionListener(_ -> {
            BufferedImage img = InputImage.loadImage();
            if (img != null) {
                currentImage = img;
                zoom = getInitialZoom(img.getWidth(), img.getHeight());
                showPreview();
            } else {
                JOptionPane.showMessageDialog(this, "No file selected.");
            }
        });

        upscale.addActionListener(_ -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Please load an image first.");
                return;
            }

            Panel4imageScaler dialog = new Panel4imageScaler((Frame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);

            String scale = dialog.getSelectedScale();
            String model = dialog.getSelectedModel();

            if (scale == null || model == null) return;

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Upscaling...");

            setButtonsEnabled(buttons, false);

            upscaleWorker = new SwingWorker<>() {
                File input, output;

                @Override
                protected BufferedImage doInBackground() throws Exception {
                    File tempDir = new File("src/main/java/Models/AI/REALESRGAN/temp_upscale");
                    if (!tempDir.exists()) tempDir.mkdirs();

                    String unique = String.valueOf(System.currentTimeMillis());
                    input = new File(tempDir, "input_" + unique + ".png");
                    output = new File(tempDir, "output_" + unique + ".png");

                    ImageIO.write(currentImage, "png", input);
                    ImageUpscaler.upscaleImage(input.getAbsolutePath(), output.getAbsolutePath(), model);

                    if (!output.exists()) throw new IOException("Output file missing: " + output.getAbsolutePath());

                    return ImageIO.read(output);
                }

                @Override
                protected void done() {
                    try {
                        if (!isCancelled()) {
                            currentImage = get();
                            zoom = getInitialZoom(currentImage.getWidth(), currentImage.getHeight());
                            showPreview();
                            JOptionPane.showMessageDialog(ImageSectionPanel.this,
                                    "✅ Upscaled with model: " + model + ", scale: " + scale + "x");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ImageSectionPanel.this, "❌ Error: " + ex.getMessage());
                    } finally {
                        if (input != null) input.delete();
                        if (output != null) output.delete();
                        progressBar.setVisible(false);
                        progressBar.setIndeterminate(false);
                        setButtonsEnabled(buttons, true);
                        upscaleWorker = null;
                    }
                }
            };
            upscaleWorker.execute();
        });

        clear.addActionListener(_ -> {
            currentImage = null;
            imageLabel.setIcon(null);
            imageLabel.setText("No image loaded");
            zoom = 1.0;
            imageLabel.revalidate();
            imageLabel.repaint();
        });

        save.addActionListener(_ -> {
            if (currentImage != null) {
                OutputImage.saveImage(currentImage);
            } else {
                JOptionPane.showMessageDialog(this, "Nothing to save.");
            }
        });

        zoomIn.addActionListener(_ -> {
            if (currentImage != null) {
                zoom = Math.min(zoom * 1.25, 8.0);
                showPreview();
            }
        });

        zoomOut.addActionListener(_ -> {
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

        add(page, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);
    }

    private void showPreview() {
        if (currentImage == null) return;
        int w = (int) (currentImage.getWidth() * zoom);
        int h = (int) (currentImage.getHeight() * zoom);
        Image scaled = getHighQualityScaledImage(currentImage, w, h);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.setText(null);
        imageLabel.setPreferredSize(new Dimension(w, h));
        imageLabel.revalidate();
    }

    private void setButtonsEnabled(JPanel panel, boolean enabled) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton b) b.setEnabled(enabled);
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
        double scale = Math.min((double) PREVIEW_W / imgW, (double) PREVIEW_H / imgH);
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

    public void cleanupOnExit() {
        File tempDir = new File("src/main/java/Models/AI/REALESRGAN/temp_upscale");
        if (tempDir.exists()) {
            for (File file : tempDir.listFiles()) {
                if (file.getName().endsWith(".png")) file.delete();
            }
        }
    }    }
