package upscaler.ui;

import com.formdev.flatlaf.FlatClientProperties;
import upscaler.model.ModelDefinition;
import upscaler.model.VideoMetadata;
import upscaler.model.VideoQualityPreset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class VideoWorkspacePanel extends JPanel {
    private final JButton openVideoButton = new JButton("Open Video");
    private final JButton chooseOutputButton = new JButton("Choose Output");
    private final JButton startRenderButton = new JButton("Render Video");
    private final JButton cancelRenderButton = new JButton("Cancel Render");
    private final JButton playResultButton = new JButton("Open Result");
    private final JButton revealOutputButton = new JButton("Reveal Output");
    private final JComboBox<ModelDefinition> modelCombo = new JComboBox<>();
    private final JComboBox<Integer> scaleCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 6, 8});
    private final JComboBox<VideoFpsOption> fpsCombo = new JComboBox<>(new VideoFpsOption[]{
            new VideoFpsOption(0, "Original FPS"),
            new VideoFpsOption(60, "60 fps"),
            new VideoFpsOption(120, "120 fps"),
            new VideoFpsOption(240, "240 fps")
    });
    private final JComboBox<VideoQualityPreset> qualityCombo = new JComboBox<>(VideoQualityPreset.values());
    private final JLabel inputLabel = new JLabel("No video loaded");
    private final JLabel outputLabel = new JLabel("Output file not selected");
    private final JLabel metadataLabel = new JLabel("Open a video to inspect codec, resolution, FPS and duration.");
    private final JLabel planLabel = new JLabel("Plan: 2x upscale • 60 fps interpolation");
    private final JLabel resultLabel = new JLabel("No render yet");
    private final JProgressBar progressBar = new JProgressBar();
    private final JTextArea logArea = new JTextArea();
    private final VideoPosterPanel posterPanel = new VideoPosterPanel();

    public VideoWorkspacePanel() {
        setOpaque(false);
        setLayout(new BorderLayout(22, 22));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel left = createCard(new BorderLayout());
        left.setPreferredSize(new Dimension(380, 0));
        left.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Video Upscaling & Motion");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel subtitle = new JLabel("Restore frames with Real-ESRGAN, shape motion cleanly and export through the encoder you selected.");
        subtitle.setForeground(UiPalette.MUTED);

        body.add(title);
        body.add(Box.createVerticalStrut(8));
        body.add(subtitle);
        body.add(Box.createVerticalStrut(20));

        body.add(sectionLabel("Input clip"));
        body.add(Box.createVerticalStrut(8));
        body.add(openVideoButton);
        body.add(Box.createVerticalStrut(10));
        body.add(inputLabel);
        body.add(Box.createVerticalStrut(4));
        body.add(metadataLabel);
        body.add(Box.createVerticalStrut(18));

        body.add(sectionLabel("Output file"));
        body.add(Box.createVerticalStrut(8));
        body.add(chooseOutputButton);
        body.add(Box.createVerticalStrut(10));
        body.add(outputLabel);
        body.add(Box.createVerticalStrut(18));

        body.add(sectionLabel("Model"));
        body.add(Box.createVerticalStrut(8));
        body.add(modelCombo);
        body.add(Box.createVerticalStrut(18));

        body.add(sectionLabel("Upscale factor"));
        body.add(Box.createVerticalStrut(8));
        body.add(scaleCombo);
        body.add(Box.createVerticalStrut(18));

        body.add(sectionLabel("Target frame rate"));
        body.add(Box.createVerticalStrut(8));
        body.add(fpsCombo);
        body.add(Box.createVerticalStrut(18));

        body.add(sectionLabel("Render quality"));
        body.add(Box.createVerticalStrut(8));
        body.add(qualityCombo);
        body.add(Box.createVerticalStrut(12));
        body.add(planLabel);
        body.add(Box.createVerticalStrut(18));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 10));
        buttons.setOpaque(false);
        buttons.add(startRenderButton);
        buttons.add(cancelRenderButton);
        body.add(buttons);
        body.add(Box.createVerticalStrut(10));

        JPanel resultActions = new JPanel(new GridLayout(1, 2, 10, 10));
        resultActions.setOpaque(false);
        resultActions.add(playResultButton);
        resultActions.add(revealOutputButton);
        body.add(resultActions);
        body.add(Box.createVerticalStrut(14));
        body.add(progressBar);
        body.add(Box.createVerticalStrut(14));
        body.add(resultLabel);
        body.add(Box.createVerticalGlue());

        left.add(body, BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        JPanel right = createCard(new BorderLayout(0, 14));
        right.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        posterPanel.setPreferredSize(new Dimension(860, 500));
        right.add(posterPanel, BorderLayout.CENTER);

        JPanel logCard = new JPanel(new BorderLayout(0, 10));
        logCard.setOpaque(false);
        JLabel logTitle = sectionLabel("Render diagnostics");
        logCard.add(logTitle, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new java.awt.Color(16, 21, 28));
        logArea.setForeground(UiPalette.TEXT);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setMargin(new Insets(12, 12, 12, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255, 24)));
        scrollPane.setPreferredSize(new Dimension(0, 220));
        logCard.add(scrollPane, BorderLayout.CENTER);
        right.add(logCard, BorderLayout.SOUTH);

        add(right, BorderLayout.CENTER);

        openVideoButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c2430;foreground:#e7edf4;arc:20;borderColor:#3a4654");
        chooseOutputButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c2430;foreground:#e7edf4;arc:20;borderColor:#3a4654");
        startRenderButton.putClientProperty(FlatClientProperties.STYLE, "background:#b8673f;foreground:#fff8f4;font:bold +1;arc:20;borderWidth:0");
        cancelRenderButton.putClientProperty(FlatClientProperties.STYLE, "background:#2e1820;foreground:#ff9fa6;arc:20;borderColor:#5a2937");
        playResultButton.putClientProperty(FlatClientProperties.STYLE, "background:#17342f;foreground:#8fe0d1;arc:20;borderColor:#27564e");
        revealOutputButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c2430;foreground:#e7edf4;arc:20;borderColor:#3a4654");
        modelCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");
        scaleCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");
        fpsCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");
        qualityCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");
        progressBar.putClientProperty(FlatClientProperties.STYLE, "arc:999;background:#171d25;foreground:#d88862");

        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setString("Idle");
        cancelRenderButton.setEnabled(false);
        playResultButton.setEnabled(false);
        revealOutputButton.setEnabled(false);

        inputLabel.setFont(inputLabel.getFont().deriveFont(Font.BOLD, 15f));
        outputLabel.setForeground(UiPalette.MUTED);
        metadataLabel.setForeground(UiPalette.MUTED);
        planLabel.setForeground(UiPalette.MUTED);
        resultLabel.setForeground(UiPalette.MUTED);
    }

    private JPanel createCard(BorderLayout layout) {
        JPanel panel = new GlassPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder());
        return panel;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setForeground(UiPalette.MUTED);
        return label;
    }

    public JButton getOpenVideoButton() {
        return openVideoButton;
    }

    public JButton getChooseOutputButton() {
        return chooseOutputButton;
    }

    public JButton getStartRenderButton() {
        return startRenderButton;
    }

    public JButton getCancelRenderButton() {
        return cancelRenderButton;
    }

    public JButton getPlayResultButton() {
        return playResultButton;
    }

    public JButton getRevealOutputButton() {
        return revealOutputButton;
    }

    public JComboBox<ModelDefinition> getModelCombo() {
        return modelCombo;
    }

    public JComboBox<Integer> getScaleCombo() {
        return scaleCombo;
    }

    public JComboBox<VideoFpsOption> getFpsCombo() {
        return fpsCombo;
    }

    public JComboBox<VideoQualityPreset> getQualityCombo() {
        return qualityCombo;
    }

    public void setModels(Iterable<ModelDefinition> models) {
        modelCombo.removeAllItems();
        for (ModelDefinition model : models) {
            modelCombo.addItem(model);
        }
    }

    public void setInputFile(Path file) {
        inputLabel.setText(file == null ? "No video loaded" : file.getFileName().toString());
    }

    public void setOutputFile(Path file) {
        outputLabel.setText(file == null ? "Output file not selected" : file.toString());
    }

    public void setMetadata(VideoMetadata metadata) {
        if (metadata == null) {
            metadataLabel.setText("Open a video to inspect codec, resolution, FPS and duration.");
            return;
        }
        metadataLabel.setText(metadata.resolutionLabel() + "  •  " + metadata.fpsLabel() + "  •  " + metadata.durationLabel() +
                "  •  video:" + metadata.videoCodec() + (metadata.hasAudio() ? "  •  audio:" + metadata.audioCodec() : "  •  muted"));
    }

    public void setPoster(BufferedImage image, VideoMetadata metadata) {
        posterPanel.setContent(image, metadata);
    }

    public void setPlanText(String text) {
        planLabel.setText("Plan: " + text);
    }

    public void setResultText(String text) {
        resultLabel.setText(text);
    }

    public void clearLog() {
        logArea.setText("");
    }

    public void appendLog(String line) {
        if (!logArea.getText().isBlank()) {
            logArea.append(System.lineSeparator());
        }
        logArea.append(line);
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void setBusy(boolean busy) {
        openVideoButton.setEnabled(!busy);
        chooseOutputButton.setEnabled(!busy);
        startRenderButton.setEnabled(!busy);
        cancelRenderButton.setEnabled(busy);
        if (busy) {
            playResultButton.setEnabled(false);
            revealOutputButton.setEnabled(false);
        }
        modelCombo.setEnabled(!busy);
        scaleCombo.setEnabled(!busy);
        fpsCombo.setEnabled(!busy);
        qualityCombo.setEnabled(!busy);
    }

    public void resetProgress() {
        progressBar.setValue(0);
    }

    public void setProgress(int percent, String text) {
        progressBar.setValue(Math.max(0, Math.min(100, percent)));
        progressBar.setString(text);
    }

    public void setResultActionsEnabled(boolean enabled) {
        playResultButton.setEnabled(enabled);
        revealOutputButton.setEnabled(enabled);
    }
}
