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

    private final JButton saveImageBtn = createButton("Save Image"); // добавляем сразу

    public ImageSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(32, 34, 37));

        JPanel leftPanel = initLeftPanel();
        JPanel previewPanel = initPreviewPanel();

        add(leftPanel, BorderLayout.WEST);
        add(previewPanel, BorderLayout.CENTER);
    }

    private JPanel initLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(new Color(25, 27, 35));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        panel.add(createTitleLabel("🔥 IMAGE UPSCALE"));

        // Step 1
        panel.add(createStepLabel("Step 1: Select Image"));
        JButton selectImage = createButton("Select Image");
        selectImage.addActionListener(e -> {
            BufferedImage img = InputImage.loadImage();
            if (img != null) {
                currentImage = img;
                previewLabel.setIcon(new ImageIcon(img.getScaledInstance(600, -1, Image.SCALE_SMOOTH)));
                previewLabel.setText(null);
                saveImageBtn.setEnabled(false); // сброс
            }
        });
        panel.add(selectImage);
        panel.add(Box.createVerticalStrut(20));

        // Step 2
        panel.add(createStepLabel("Step 2: Select AI Model"));
        JComboBox<String> modelBox = new JComboBox<>(new String[]{
                "realesrgan-x4plus",
                "realesrgan-x4plus-anime"
        });
        modelBox.setBackground(new Color(44, 47, 54));
        modelBox.setForeground(Color.WHITE);
        modelBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        modelBox.setMaximumSize(new Dimension(240, 32));
        modelBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(modelBox);

        panel.add(Box.createVerticalStrut(30));
        panel.add(createStepLabel("Step 3: Start Upscaling"));

        JButton upscaleBtn = createButton("Start Upscaling");
        upscaleBtn.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Please select an image first.");
                return;
            }

            File tempInput = new File("image_input.png");
            File tempOutput = new File("image_output.png");

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
                    ImageUpscaler.upscaleImage(tempInput.getAbsolutePath(), tempOutput.getAbsolutePath(), modelBox.getSelectedItem().toString());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        currentImage = ImageIO.read(tempOutput);
                        Image scaled = currentImage.getScaledInstance(600, -1, Image.SCALE_SMOOTH);
                        previewLabel.setIcon(new ImageIcon(scaled));
                        previewLabel.setText("");
                        progressBar.setIndeterminate(false);
                        progressBar.setString("Finished");
                        saveImageBtn.setEnabled(true); // АКТИВАЦИЯ Сохранить
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ImageSectionPanel.this, "❌ Failed to upscale: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        panel.add(upscaleBtn);

        panel.add(Box.createVerticalStrut(20));
        panel.add(saveImageBtn);
        saveImageBtn.setEnabled(false); // Запрещена до апскейла

        saveImageBtn.addActionListener(e -> {
            if (currentImage != null) {
                OutputImage.saveImage(currentImage); // 👇 твой метод сохраняет
            } else {
                JOptionPane.showMessageDialog(this, "No image to save.");
            }
        });

        panel.add(Box.createVerticalStrut(10));
        progressBar.setVisible(false);
        panel.add(progressBar);

        return panel;
    }

    private JPanel initPreviewPanel() {
        previewLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        previewLabel.setForeground(Color.GRAY);
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        previewPanel.setBackground(new Color(35, 36, 42));
        return previewPanel;
    }

    private JLabel createStepLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return label;
    }

    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(new Color(120, 180, 255));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        return label;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(new Color(60, 63, 72));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(240, 36));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }
}