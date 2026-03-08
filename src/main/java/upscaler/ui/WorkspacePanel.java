package upscaler.ui;

import com.formdev.flatlaf.FlatClientProperties;
import upscaler.model.ModelDefinition;

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
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.nio.file.Path;

public class WorkspacePanel extends JPanel {
    private final JButton openButton = new JButton("Open Image");
    private final JButton startButton = new JButton("Upscale");
    private final JButton cancelButton = new JButton("Cancel");
    private final JButton saveButton = new JButton("Save Result");
    private final JComboBox<ModelDefinition> modelCombo = new JComboBox<>();
    private final JComboBox<Integer> scaleCombo = new JComboBox<>(new Integer[]{2, 3, 4, 6, 8});
    private final JLabel fileLabel = new JLabel("No image loaded");
    private final JLabel fileMetaLabel = new JLabel("Load a photo, texture or artwork to begin.");
    private final JLabel deviceLabel = new JLabel("Compute device: Auto");
    private final JLabel pipelineLabel = new JLabel("Pipeline: 4x");
    private final JLabel resultMetaLabel = new JLabel("Result preview will appear here.");
    private final JProgressBar progressBar = new JProgressBar();
    private final JTextArea logArea = new JTextArea();
    private final ComparePreviewPanel previewPanel = new ComparePreviewPanel();

    public WorkspacePanel() {
        setOpaque(false);
        setLayout(new BorderLayout(22, 22));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sidebar = createSidebar();
        sidebar.setPreferredSize(new Dimension(360, 0));
        add(sidebar, BorderLayout.WEST);

        JPanel previewCard = createCard(new BorderLayout(0, 16));
        previewCard.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JPanel previewHeader = new JPanel(new BorderLayout());
        previewHeader.setOpaque(false);

        JLabel title = new JLabel("Compare View");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        previewHeader.add(title, BorderLayout.WEST);

        JLabel hint = new JLabel("Drag the divider to compare before and after");
        hint.setForeground(UiPalette.MUTED);
        previewHeader.add(hint, BorderLayout.EAST);

        previewCard.add(previewHeader, BorderLayout.NORTH);
        previewCard.add(previewPanel, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        resultMetaLabel.setForeground(UiPalette.MUTED);
        bottomBar.add(resultMetaLabel, BorderLayout.WEST);
        previewCard.add(bottomBar, BorderLayout.SOUTH);

        add(previewCard, BorderLayout.CENTER);

        startButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c8074;foreground:#f7fbfb;font:bold +1;arc:20;borderWidth:0");
        cancelButton.putClientProperty(FlatClientProperties.STYLE, "background:#2e1820;foreground:#ff9fa6;arc:20;borderColor:#5a2937");
        saveButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c2430;foreground:#e7edf4;arc:20;borderColor:#3a4654");
        openButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c2430;foreground:#e7edf4;arc:20;borderColor:#3a4654");
        progressBar.putClientProperty(FlatClientProperties.STYLE, "arc:999;background:#171d25;foreground:#56c6b2");
        modelCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");
        scaleCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");

        cancelButton.setEnabled(false);
        saveButton.setEnabled(false);
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setString("Idle");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JPanel hero = createCard(new BorderLayout(0, 12));
        hero.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel badge = new JLabel("WORKSPACE", SwingConstants.LEFT);
        badge.setForeground(UiPalette.WARM);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 12f));

        JLabel title = new JLabel("Upscale without guessing what the engine is doing");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        JLabel subtitle = new JLabel("Exact scale plans, visible runtime state, clean diagnostics and real before/after control.");
        subtitle.setForeground(UiPalette.MUTED);

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        heroText.add(badge);
        heroText.add(Box.createVerticalStrut(8));
        heroText.add(title);
        heroText.add(Box.createVerticalStrut(10));
        heroText.add(subtitle);

        hero.add(heroText, BorderLayout.CENTER);
        sidebar.add(hero);
        sidebar.add(Box.createVerticalStrut(18));

        JPanel controls = createCard(new BorderLayout());
        controls.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(createSectionTitle("Source"));
        body.add(Box.createVerticalStrut(8));
        body.add(openButton);
        body.add(Box.createVerticalStrut(10));
        body.add(fileLabel);
        body.add(Box.createVerticalStrut(4));
        body.add(fileMetaLabel);
        body.add(Box.createVerticalStrut(18));

        body.add(createSectionTitle("AI Model"));
        body.add(Box.createVerticalStrut(8));
        body.add(modelCombo);
        body.add(Box.createVerticalStrut(18));

        body.add(createSectionTitle("Output Scale"));
        body.add(Box.createVerticalStrut(8));
        body.add(scaleCombo);
        body.add(Box.createVerticalStrut(10));
        body.add(pipelineLabel);
        body.add(Box.createVerticalStrut(18));

        body.add(createSectionTitle("Runtime"));
        body.add(Box.createVerticalStrut(8));
        body.add(deviceLabel);
        body.add(Box.createVerticalStrut(16));

        JPanel actionRow = new JPanel(new GridLayout(2, 2, 10, 10));
        actionRow.setOpaque(false);
        actionRow.add(startButton);
        actionRow.add(cancelButton);
        actionRow.add(saveButton);
        actionRow.add(Box.createGlue());
        body.add(actionRow);
        body.add(Box.createVerticalStrut(16));
        body.add(progressBar);

        controls.add(body, BorderLayout.CENTER);
        sidebar.add(controls);
        sidebar.add(Box.createVerticalStrut(18));

        JPanel diagnostics = createCard(new BorderLayout(0, 10));
        diagnostics.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel diagnosticsTitle = createSectionTitle("Diagnostics");
        diagnostics.add(diagnosticsTitle, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(10, 10, 10, 10));
        logArea.setBackground(new Color(16, 21, 28));
        logArea.setForeground(UiPalette.TEXT);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 24)));
        scrollPane.setPreferredSize(new Dimension(320, 180));
        diagnostics.add(scrollPane, BorderLayout.CENTER);
        sidebar.add(diagnostics);
        sidebar.add(Box.createVerticalGlue());

        styleLabel(fileLabel, true);
        styleLabel(fileMetaLabel, false);
        styleLabel(deviceLabel, false);
        styleLabel(pipelineLabel, false);
        styleLabel(resultMetaLabel, false);

        return sidebar;
    }

    private JPanel createCard(BorderLayout layout) {
        JPanel panel = new GlassPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder());
        return panel;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setForeground(UiPalette.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleLabel(JLabel label, boolean primary) {
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setForeground(primary ? UiPalette.TEXT : UiPalette.MUTED);
        if (primary) {
            label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        }
    }

    public JButton getOpenButton() {
        return openButton;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JComboBox<ModelDefinition> getModelCombo() {
        return modelCombo;
    }

    public JComboBox<Integer> getScaleCombo() {
        return scaleCombo;
    }

    public ComparePreviewPanel getPreviewPanel() {
        return previewPanel;
    }

    public void setFile(Path file, int width, int height) {
        fileLabel.setText(file.getFileName().toString());
        fileMetaLabel.setText(width + " × " + height + " px");
    }

    public void clearFile() {
        fileLabel.setText("No image loaded");
        fileMetaLabel.setText("Load a photo, texture or artwork to begin.");
    }

    public void setSelectedDeviceText(String text) {
        deviceLabel.setText("Compute device: " + text);
    }

    public void setPipelineText(String text) {
        pipelineLabel.setText("Pipeline: " + text);
    }

    public void setBusy(boolean busy) {
        startButton.setEnabled(!busy);
        cancelButton.setEnabled(busy);
        openButton.setEnabled(!busy);
        modelCombo.setEnabled(!busy);
        scaleCombo.setEnabled(!busy);
        progressBar.setIndeterminate(false);
    }

    public void setSaveEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    public void setStatusText(String text) {
        progressBar.setString(text);
    }

    public void resetProgress() {
        progressBar.setValue(0);
    }

    public void setProgressValue(int value) {
        progressBar.setValue(Math.max(0, Math.min(100, value)));
    }

    public void setResultMetaText(String text) {
        resultMetaLabel.setText(text);
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

    public void setModels(Iterable<ModelDefinition> models) {
        modelCombo.removeAllItems();
        for (ModelDefinition model : models) {
            modelCombo.addItem(model);
        }
    }

    public void setPreviewImages(java.awt.image.BufferedImage beforeImage, java.awt.image.BufferedImage afterImage) {
        previewPanel.setImages(beforeImage, afterImage);
        previewPanel.resetDivider();
    }
}
