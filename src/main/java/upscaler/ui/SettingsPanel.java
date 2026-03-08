package upscaler.ui;

import com.formdev.flatlaf.FlatClientProperties;
import upscaler.config.AppDirectories;
import upscaler.model.ComputeDevice;
import upscaler.model.VideoEncoderOption;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class SettingsPanel extends JPanel {
    private final JComboBox<ComputeDevice> deviceCombo = new JComboBox<>();
    private final JButton refreshDevicesButton = new JButton("Refresh Devices");
    private final JLabel deviceDetailsLabel = new JLabel("Auto selection uses the first healthy Vulkan device.");
    private final JSpinner tileSizeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 2048, 32));
    private final JComboBox<String> threadProfileCombo = new JComboBox<>(new String[]{"1:2:2", "1:3:3", "2:4:2", "2:4:4"});
    private final JCheckBox ttaCheckBox = new JCheckBox("Enable TTA for maximum quality when you accept slower processing");
    private final JComboBox<VideoEncoderOption> videoEncoderCombo = new JComboBox<>();
    private final JLabel videoEncoderDetailsLabel = new JLabel("Auto keeps the most stable software encode path by default.");
    private final JButton openModelsButton = new JButton("Open Models Folder");
    private final JButton reloadModelsButton = new JButton("Reload Models");
    private final JButton openAppDataButton = new JButton("Open App Data");
    private final JTextArea diagnosticsArea = new JTextArea();
    private final JLabel runtimePathLabel = new JLabel();
    private final JLabel modelsPathLabel = new JLabel();

    public SettingsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(22, 22));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel left = createCard();
        left.setPreferredSize(new Dimension(430, 0));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel title = new JLabel("Runtime Settings");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);
        left.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Hardware selection and engine knobs that map to the bundled Real-ESRGAN runtime.");
        subtitle.setForeground(UiPalette.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(subtitle);
        left.add(Box.createVerticalStrut(20));

        left.add(sectionLabel("Compute device"));
        left.add(Box.createVerticalStrut(8));
        deviceCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        deviceCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");
        left.add(deviceCombo);
        left.add(Box.createVerticalStrut(8));
        refreshDevicesButton.putClientProperty(FlatClientProperties.STYLE, "background:#17342f;foreground:#8fe0d1;arc:18;borderColor:#27564e");
        refreshDevicesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(refreshDevicesButton);
        deviceDetailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        deviceDetailsLabel.setForeground(UiPalette.MUTED);
        left.add(Box.createVerticalStrut(6));
        left.add(deviceDetailsLabel);
        left.add(Box.createVerticalStrut(20));

        left.add(sectionLabel("Tile size"));
        left.add(Box.createVerticalStrut(8));
        left.add(tileSizeSpinner);
        left.add(Box.createVerticalStrut(18));

        left.add(sectionLabel("Thread profile"));
        left.add(Box.createVerticalStrut(8));
        threadProfileCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");
        left.add(threadProfileCombo);
        left.add(Box.createVerticalStrut(18));

        ttaCheckBox.setOpaque(false);
        ttaCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(ttaCheckBox);
        left.add(Box.createVerticalStrut(20));

        left.add(sectionLabel("Video encoder"));
        left.add(Box.createVerticalStrut(8));
        videoEncoderCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        videoEncoderCombo.putClientProperty(FlatClientProperties.STYLE, "arc:18");
        left.add(videoEncoderCombo);
        videoEncoderDetailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        videoEncoderDetailsLabel.setForeground(UiPalette.MUTED);
        left.add(Box.createVerticalStrut(6));
        left.add(videoEncoderDetailsLabel);
        left.add(Box.createVerticalStrut(20));

        left.add(sectionLabel("Paths"));
        left.add(Box.createVerticalStrut(8));
        runtimePathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        modelsPathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        runtimePathLabel.setForeground(UiPalette.MUTED);
        modelsPathLabel.setForeground(UiPalette.MUTED);
        left.add(runtimePathLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(modelsPathLabel);
        left.add(Box.createVerticalStrut(12));

        openModelsButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c2430;foreground:#e7edf4;arc:18;borderColor:#3a4654");
        reloadModelsButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c2430;foreground:#e7edf4;arc:18;borderColor:#3a4654");
        openAppDataButton.putClientProperty(FlatClientProperties.STYLE, "background:#1c2430;foreground:#e7edf4;arc:18;borderColor:#3a4654");

        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);
        buttons.add(openModelsButton);
        buttons.add(reloadModelsButton);
        buttons.add(openAppDataButton);
        buttons.add(Box.createGlue());
        left.add(buttons);
        left.add(Box.createVerticalGlue());

        JPanel right = createCard();
        right.setLayout(new BorderLayout(0, 12));
        right.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel diagnosticsTitle = new JLabel("Detection & Engine Notes");
        diagnosticsTitle.setFont(diagnosticsTitle.getFont().deriveFont(Font.BOLD, 20f));
        right.add(diagnosticsTitle, BorderLayout.NORTH);

        diagnosticsArea.setEditable(false);
        diagnosticsArea.setLineWrap(true);
        diagnosticsArea.setWrapStyleWord(true);
        diagnosticsArea.setBackground(new java.awt.Color(16, 21, 28));
        diagnosticsArea.setForeground(UiPalette.TEXT);
        diagnosticsArea.setMargin(new java.awt.Insets(14, 14, 14, 14));

        JScrollPane scrollPane = new JScrollPane(diagnosticsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255, 24)));
        right.add(scrollPane, BorderLayout.CENTER);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);

        runtimePathLabel.setText("Runtime: " + AppDirectories.runtimeDirectory());
        modelsPathLabel.setText("Models: " + AppDirectories.userModelsDirectory());
    }

    private JPanel createCard() {
        JPanel panel = new GlassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder());
        return panel;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setForeground(UiPalette.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public JComboBox<ComputeDevice> getDeviceCombo() {
        return deviceCombo;
    }

    public JButton getRefreshDevicesButton() {
        return refreshDevicesButton;
    }

    public JSpinner getTileSizeSpinner() {
        return tileSizeSpinner;
    }

    public JComboBox<String> getThreadProfileCombo() {
        return threadProfileCombo;
    }

    public JCheckBox getTtaCheckBox() {
        return ttaCheckBox;
    }

    public JComboBox<VideoEncoderOption> getVideoEncoderCombo() {
        return videoEncoderCombo;
    }

    public JButton getOpenModelsButton() {
        return openModelsButton;
    }

    public JButton getReloadModelsButton() {
        return reloadModelsButton;
    }

    public JButton getOpenAppDataButton() {
        return openAppDataButton;
    }

    public void setDiagnosticsText(String text) {
        diagnosticsArea.setText(text);
        diagnosticsArea.setCaretPosition(0);
    }

    public void setDeviceDetailsText(String text) {
        deviceDetailsLabel.setText(text);
    }

    public void setVideoEncoders(Iterable<VideoEncoderOption> encoders) {
        videoEncoderCombo.removeAllItems();
        for (VideoEncoderOption encoder : encoders) {
            videoEncoderCombo.addItem(encoder);
        }
    }

    public void setVideoEncoderDetailsText(String text) {
        videoEncoderDetailsLabel.setText(text);
    }
}
