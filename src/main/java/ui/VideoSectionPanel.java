package ui;

import VideoScaler.InputVideo;
import VideoScaler.OutputVideo;
import VideoScaler.VideoUpscaler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class VideoSectionPanel extends JPanel {

    private File currentVideo;
    private final JLabel videoLabel = new JLabel("No video loaded", SwingConstants.CENTER);
    private final JProgressBar progressBar = new JProgressBar();

    public VideoSectionPanel() {
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

        panel.add(createTitleLabel());

        // Step 1
        panel.add(createStepLabel("Step 1: Select Video"));
        JButton selectBtn = createButton("Select Video");
        selectBtn.addActionListener(_ -> {
            File file = InputVideo.loadVideo();
            if (file != null) {
                currentVideo = file;
                videoLabel.setText("🎥 " + file.getName());
            }
        });
        panel.add(selectBtn);

        panel.add(Box.createVerticalStrut(20));

        // Step 2
        panel.add(createStepLabel("Step 2: Select AI Model"));
        JComboBox<String> modelBox = new JComboBox<>(new String[]{
                "realesr-animevideov3-x2",
                "realesr-animevideov3-x3",
                "realesr-animevideov3-x4"
        });
        modelBox.setBackground(new Color(44, 47, 54));
        modelBox.setForeground(Color.WHITE);
        modelBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        modelBox.setMaximumSize(new Dimension(240, 32));
        panel.add(modelBox);

        panel.add(Box.createVerticalStrut(30));

        // Step 3
        panel.add(createStepLabel("Step 3: Upscale to..."));
        JButton upscaleBtn = createButton("Start Upscaling");

        upscaleBtn.addActionListener(_ -> {
            if (currentVideo == null) {
                JOptionPane.showMessageDialog(this, "Select a video file first.");
                return;
            }

            File output = OutputVideo.chooseSaveLocation("upscaled_video.mp4");
            if (output == null) return;

            String model = Objects.requireNonNull(modelBox.getSelectedItem()).toString();

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Upscaling...");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        VideoUpscaler.upscaleFromVideoFile(
                                currentVideo.getAbsolutePath(),
                                output.getAbsolutePath(),
                                model
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(VideoSectionPanel.this, "❌ Failed: " + ex.getMessage());
                    }
                    return null;
                }

                @Override
                protected void done() {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    progressBar.setString("Done");
                    JOptionPane.showMessageDialog(VideoSectionPanel.this,
                            "✅ Upscaled video saved:\n" + output.getAbsolutePath());
                }
            }.execute();
        });

        panel.add(upscaleBtn);
        panel.add(Box.createVerticalStrut(20));
        panel.add(progressBar);

        return panel;
    }

    private JPanel initPreviewPanel() {
        videoLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        videoLabel.setForeground(Color.LIGHT_GRAY);
        videoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        panel.add(videoLabel, BorderLayout.CENTER);
        panel.setBackground(new Color(35, 36, 42));
        return panel;
    }

    private JLabel createStepLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return label;
    }

    private JLabel createTitleLabel() {
        JLabel label = new JLabel("🎞️ VIDEO UPSCALE");
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(new Color(120, 180, 255));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
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