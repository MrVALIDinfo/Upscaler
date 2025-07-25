package ui;

import VideoScaler.InputVideo;
import VideoScaler.OutputVideo;
import VideoScaler.VideoUpscaler;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VideoSectionPanel extends JPanel {

    private final JLabel videoLabel = new JLabel("No video loaded", SwingConstants.CENTER);
    private final JProgressBar progressBar;
    private File currentVideo;
    private double zoom = 1.0;

    private static final int PREVIEW_W = 800;
    private static final int PREVIEW_H = 600;

    public VideoSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(40, 44, 52));

        JLabel title = new JLabel("Upscale Video", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        videoLabel.setVerticalAlignment(SwingConstants.CENTER);
        videoLabel.setForeground(Color.GRAY);

        JScrollPane previewScroll = new JScrollPane(videoLabel);
        previewScroll.setPreferredSize(new Dimension(PREVIEW_W, PREVIEW_H));
        previewScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65)));
        previewScroll.getViewport().setBackground(new Color(30, 33, 36));

        progressBar = new JProgressBar(0, 100);
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

        // Zoom отключаем - не нужен сейчас для видео превью
        buttons.add(upload);
        buttons.add(upscale);
        buttons.add(clear);
        buttons.add(save);

        upload.addActionListener(_ -> {
            File file = InputVideo.loadVideo();
            if (file != null) {
                currentVideo = file;
                videoLabel.setText("🎥 Loaded: " + file.getName());
                videoLabel.setForeground(Color.LIGHT_GRAY);
                generateThumbnail(file);
            }
        });

        upscale.addActionListener(_ -> {
            if (currentVideo == null) {
                JOptionPane.showMessageDialog(this, "Please load a video first.");
                return;
            }

            if (!isFFmpegAvailable()) {
                JOptionPane.showMessageDialog(this, "⚠ FFmpeg is not available in PATH.");
                return;
            }

            Panel4videoScaler dialog = new Panel4videoScaler((Frame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            String selectedModel = dialog.getSelectedModel();

            if (selectedModel == null) return;

            File outputFile = OutputVideo.chooseSaveLocation("upscaled_video.mp4");
            if (outputFile == null) return;

            progressBar.setVisible(true);
            progressBar.setValue(0);
            progressBar.setString("Upscaling...");

            setButtonsEnabled(buttons, false);

            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    VideoUpscaler.upscaleVideo(
                            currentVideo.getAbsolutePath(),
                            outputFile.getAbsolutePath(),
                            selectedModel,
                            this::publish
                    );
                    return null;
                }

                @Override
                protected void process(java.util.List<Integer> chunks) {
                    int last = chunks.get(chunks.size() - 1);
                    progressBar.setValue(last);
                    progressBar.setString("Upscaling... " + last + "%");
                }

                @Override
                protected void done() {
                    progressBar.setValue(100);
                    progressBar.setString("Done!");
                    setButtonsEnabled(buttons, true);
                    JOptionPane.showMessageDialog(VideoSectionPanel.this,
                            "🎉 Upscaling complete:\n" + outputFile.getName());
                }
            };
            worker.execute();
        });

        clear.addActionListener(_ -> {
            currentVideo = null;
            videoLabel.setText("No video loaded");
            videoLabel.setIcon(null);
            videoLabel.setForeground(Color.GRAY);
            zoom = 1.0;
        });

        save.addActionListener(_ -> {
            JOptionPane.showMessageDialog(this, "Use 'Upscale' to generate new result.");
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

    private void setButtonsEnabled(JPanel panel, boolean enabled) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton b) b.setEnabled(enabled);
        }
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

    private void generateThumbnail(File video) {
        try {
            File outputImage = new File("frame_preview.png");
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", video.getAbsolutePath(),
                    "-ss", "00:00:01", "-vframes", "1", "-y",
                    outputImage.getAbsolutePath()
            );
            pb.inheritIO().start().waitFor();

            if (outputImage.exists()) {
                ImageIcon icon = new ImageIcon(outputImage.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(320, 180, Image.SCALE_SMOOTH);
                videoLabel.setIcon(new ImageIcon(scaled));
                videoLabel.setText(null);
            }
        } catch (Exception e) {
            System.err.println("⚠ Failed to generate thumbnail: " + e.getMessage());
        }
    }

    private boolean isFFmpegAvailable() {
        try {
            Process process = new ProcessBuilder("ffmpeg", "-version").start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}