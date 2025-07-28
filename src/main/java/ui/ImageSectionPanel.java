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

        // LEFT (steps panel)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(new Color(25, 27, 35));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        leftPanel.add(createTitleLabel("🔥 IMAGE UPSCALE"));

        // Step 1: Select Image
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
        leftPanel.add(Box.createVerticalStrut(20));

        // Step 2: Select Model
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

        // Step 3: Set Scale
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
        scaleSlider.setOpaque(false);
        scaleSlider.setMaximumSize(new Dimension(240, 40));
        scaleSlider.addChangeListener(e -> {
            scaleMultiplier = scaleSlider.getValue();
            scaleLabel.setText("Image Scale: x" + scaleMultiplier);
        });
        leftPanel.add(scaleSlider);
        leftPanel.add(Box.createVerticalStrut(30));

        // Step 4: Start Upscaling
        leftPanel.add(createStepLabel("Step 4: Start"));
        JButton upscaleBtn = createButton("Start Upscaling");
        upscaleBtn.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Please select an image first.");
                return;
            }

            File tempInput = new File("image_input.png");
            final File[] output = {new File("image_output.png")};

            try {
                ImageIO.write(currentImage, "png", tempInput);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Upscaling...");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    File input = tempInput;
                    for (int i = 0; i < getRepeatCount(scaleMultiplier); i++) {
                        ImageUpscaler.upscaleImage(input.getAbsolutePath(), output[0].getAbsolutePath(), modelBox.getSelectedItem().toString());

                        // Установка текущего output как следующий input
                        input = output[0];
                        output[0] = new File("image_output_round_" + (i + 1) + ".png");
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        currentImage = ImageIO.read(new File("image_output_round_" + (getRepeatCount(scaleMultiplier) - 1) + ".png"));
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
        });
        leftPanel.add(upscaleBtn);

        // Step 5: Save
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(createStepLabel("Step 5: Save Image"));
        saveImageBtn.setEnabled(false);
        saveImageBtn.addActionListener(e -> {
            if (currentImage != null) {
                OutputImage.saveImage(currentImage);
            }
        });
        leftPanel.add(saveImageBtn);
        leftPanel.add(Box.createVerticalStrut(20));
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        leftPanel.add(progressBar);

        // PREVIEW + ZOOM
        previewLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        previewLabel.setForeground(Color.GRAY);
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(new Color(35, 36, 42));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        // Zoom panel
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
            zoom = Math.max(zoom / 1.25, 0.25);
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