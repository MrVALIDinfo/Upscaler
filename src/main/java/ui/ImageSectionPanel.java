package ui;

import ImageScaler.ImageUpscaler;
import ImageScaler.InputImage;
import ImageScaler.OutputImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageSectionPanel extends JPanel {

    private BufferedImage currentImage;
    private final JLabel previewLabel = new JLabel("No image loaded", SwingConstants.CENTER);
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton saveImageBtn = createButton("Save Image");
    private double zoom = 1.0;
    private int scaleMultiplier = 4;

    public ImageSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(32, 34, 37));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(new Color(25, 27, 35));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));

        leftPanel.add(createTitleLabel("🔥 IMAGE UPSCALE"));

        leftPanel.add(createStepLabel("Step 1: Select Image"));
        JButton selectImageBtn = createButton("Select Image");
        selectImageBtn.addActionListener(e -> {
            BufferedImage img = InputImage.loadImage();
            if (img != null) {
                currentImage = img;
                zoom = getInitialZoom(img.getWidth(), img.getHeight());
                updatePreview();
                saveImageBtn.setEnabled(false);
            }
        });
        leftPanel.add(selectImageBtn);
        leftPanel.add(Box.createVerticalStrut(25));

        leftPanel.add(createStepLabel("Step 2: Select AI Model"));
        JComboBox<String> modelBox = new JComboBox<>(new String[]{
                "realesrgan-x4plus",
                "realesrgan-x4plus-anime"
        });
        modelBox.setBackground(new Color(44, 47, 54));
        modelBox.setForeground(Color.WHITE);
        modelBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        modelBox.setMaximumSize(new Dimension(240, 32));
        modelBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(modelBox);
        leftPanel.add(Box.createVerticalStrut(25));

        leftPanel.add(createStepLabel("Step 3: Set Scale"));
        JLabel scaleLabel = new JLabel("Image Scale: x4");
        scaleLabel.setForeground(Color.LIGHT_GRAY);
        scaleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        scaleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(scaleLabel);

        JSlider scaleSlider = new JSlider(2, 10, 4);
        scaleSlider.setMajorTickSpacing(1);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.setBackground(new Color(25, 27, 35));
        scaleSlider.setForeground(Color.GRAY);
        scaleSlider.setMaximumSize(new Dimension(240, 40));
        scaleSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        scaleSlider.setFocusable(false);
        scaleSlider.putClientProperty("Slider.paintThumbArrowShape", true);
        scaleSlider.putClientProperty("JComponent.roundRect", true);
        scaleSlider.addChangeListener(e -> {
            scaleMultiplier = scaleSlider.getValue();
            scaleLabel.setText("Image Scale: x" + scaleMultiplier);
        });
        leftPanel.add(scaleSlider);
        leftPanel.add(Box.createVerticalStrut(25));

        leftPanel.add(createStepLabel("Step 4: Start"));
        JButton upscaleBtn = createButton("Start Upscaling");
        upscaleBtn.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Please select an image first.");
                return;
            }

            try {
                File input = File.createTempFile("image_input", ".png");
                File output = File.createTempFile("image_output", ".png");
                input.deleteOnExit();
                output.deleteOnExit();

                ImageIO.write(currentImage, "png", input);

                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
                progressBar.setString("Upscaling...");

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        File in = input;
                        File out = output;
                        for (int i = 0; i < getRepeatCount(scaleMultiplier); i++) {
                            ImageUpscaler.upscaleImage(in.getAbsolutePath(), out.getAbsolutePath(), modelBox.getSelectedItem().toString());
                            if (i < getRepeatCount(scaleMultiplier) - 1) {
                                in = out;
                                out = File.createTempFile("image_upscale_round_" + i, ".png");
                                out.deleteOnExit();
                            }
                        }
                        currentImage = ImageIO.read(out);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            zoom = getInitialZoom(currentImage.getWidth(), currentImage.getHeight());
                            updatePreview();
                            progressBar.setIndeterminate(false);
                            progressBar.setString("Finished");
                            saveImageBtn.setEnabled(true);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ImageSectionPanel.this, "❌ Failed: " + ex.getMessage());
                        }
                    }
                }.execute();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "⚠ Error during processing.");
            }
        });
        leftPanel.add(upscaleBtn);
        leftPanel.add(Box.createVerticalStrut(25));

        leftPanel.add(createStepLabel("Step 5: Save Image"));
        saveImageBtn.setEnabled(false);
        saveImageBtn.addActionListener(e -> {
            if (currentImage != null) {
                OutputImage.saveImage(currentImage);
            }
        });
        leftPanel.add(saveImageBtn);
        leftPanel.add(Box.createVerticalStrut(25));

        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        leftPanel.add(progressBar);
        leftPanel.add(Box.createVerticalGlue());

        previewLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        previewLabel.setForeground(Color.GRAY);
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(new Color(35, 36, 42));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        zoomPanel.setOpaque(false);

        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("–");

        zoomIn.setPreferredSize(new Dimension(36, 32));
        zoomOut.setPreferredSize(new Dimension(36, 32));
        zoomIn.putClientProperty("JButton.buttonType", "roundRect");
        zoomOut.putClientProperty("JButton.buttonType", "roundRect");

        zoomIn.setBackground(new Color(60, 63, 72));
        zoomOut.setBackground(new Color(60, 63, 72));
        zoomIn.setForeground(Color.WHITE);
        zoomOut.setForeground(Color.WHITE);
        zoomIn.setFont(new Font("SansSerif", Font.BOLD, 14));
        zoomOut.setFont(new Font("SansSerif", Font.BOLD, 14));

        zoomIn.addActionListener(e -> {
            zoom = Math.min(zoom * 1.25, 8.0);
            updatePreview();
        });

        zoomOut.addActionListener(e -> {
            zoom = Math.max(zoom / 1.25, getInitialZoom(currentImage.getWidth(), currentImage.getHeight()));
            updatePreview();
        });

        zoomPanel.add(zoomOut);
        zoomPanel.add(zoomIn);
        previewPanel.add(zoomPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(previewPanel, BorderLayout.CENTER);
    }

    private JLabel createTitleLabel(String txt) {
        JLabel label = new JLabel(txt);
        label.setForeground(new Color(120, 180, 255));
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        return label;
    }

    private JLabel createStepLabel(String txt) {
        JLabel label = new JLabel(txt);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return label;
    }

    private JButton createButton(String txt) {
        JButton btn = new JButton(txt);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(new Color(60, 63, 72));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(240, 36));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return btn;
    }

    private void updatePreview() {
        if (currentImage == null) return;
        int w = (int) (currentImage.getWidth() * zoom);
        int h = (int) (currentImage.getHeight() * zoom);
        if (w > 8000 || h > 8000) {
            JOptionPane.showMessageDialog(this, "⚠ Image too large to preview. Try zooming out.");
            return;
        }
        Image scaled = currentImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        previewLabel.setIcon(new ImageIcon(scaled));
        previewLabel.setText(null);
    }

    private double getInitialZoom(int w, int h) {
        double sw = 800.0 / w;
        double sh = 600.0 / h;
        return Math.min(1.0, Math.min(sw, sh));
    }

    private int getRepeatCount(int targetScale) {
        int count = 0;
        int scale = 1;
        while (scale < targetScale) {
            scale *= 4;
            count++;
        }
        return count;
    }
}