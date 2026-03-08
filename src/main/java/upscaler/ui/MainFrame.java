package upscaler.ui;

import upscaler.app.UpscalerApplication;
import upscaler.config.AppDirectories;
import upscaler.config.AppSettings;
import upscaler.config.SettingsRepository;
import upscaler.model.ComputeDevice;
import upscaler.model.ModelDefinition;
import upscaler.model.ScalePlan;
import upscaler.model.UpscaleRequest;
import upscaler.model.UpscaleResult;
import upscaler.model.VideoEncoderOption;
import upscaler.model.VideoMetadata;
import upscaler.model.VideoQualityPreset;
import upscaler.model.VideoUpscaleRequest;
import upscaler.model.VideoUpscaleResult;
import upscaler.runtime.PlatformDetector;
import upscaler.service.DeviceDetectionService;
import upscaler.service.DeviceScanResult;
import upscaler.service.ModelRegistry;
import upscaler.service.RealEsrganService;
import upscaler.service.ScalePlanner;
import upscaler.service.UpscaleCancelledException;
import upscaler.service.UpscaleProgressListener;
import upscaler.service.VideoMetadataService;
import upscaler.service.VideoProcessingService;
import upscaler.service.VideoProgressListener;
import upscaler.service.VideoToolingService;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame {
    private static final String IMAGE_STATUS_PREFIX = "__IMAGE_STATUS__::";
    private static final String VIDEO_STAGE_PREFIX = "__VIDEO_STAGE__::";

    private final AppSettings settings;
    private final SettingsRepository settingsRepository;
    private final ModelRegistry modelRegistry;
    private final DeviceDetectionService deviceDetectionService;
    private final RealEsrganService upscaleService;
    private final VideoToolingService videoToolingService;
    private final VideoMetadataService videoMetadataService;
    private final VideoProcessingService videoProcessingService;

    private final WorkspacePanel workspacePanel = new WorkspacePanel();
    private final VideoWorkspacePanel videoWorkspacePanel = new VideoWorkspacePanel();
    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel detailsLabel = new JLabel();
    private final JLabel heroDeviceBadge = new JLabel("Auto");
    private final JLabel heroToolsBadge = new JLabel("Image + Video");
    private final JTabbedPane tabs = new JTabbedPane();

    private Path currentInputFile;
    private BufferedImage inputImage;
    private BufferedImage outputImage;
    private SwingWorker<UpscaleResult, String> currentImageWorker;

    private Path currentVideoFile;
    private Path currentVideoOutputFile;
    private VideoMetadata currentVideoMetadata;
    private BufferedImage currentVideoThumbnail;
    private SwingWorker<VideoUpscaleResult, String> currentVideoWorker;

    private boolean syncingUi;
    private int activeImagePass = 1;
    private int totalImagePasses = 1;

    public MainFrame(
            AppSettings settings,
            SettingsRepository settingsRepository,
            ModelRegistry modelRegistry,
            DeviceDetectionService deviceDetectionService,
            RealEsrganService upscaleService,
            VideoToolingService videoToolingService,
            VideoMetadataService videoMetadataService,
            VideoProcessingService videoProcessingService
    ) {
        this.settings = Objects.requireNonNull(settings);
        this.settingsRepository = Objects.requireNonNull(settingsRepository);
        this.modelRegistry = Objects.requireNonNull(modelRegistry);
        this.deviceDetectionService = Objects.requireNonNull(deviceDetectionService);
        this.upscaleService = Objects.requireNonNull(upscaleService);
        this.videoToolingService = Objects.requireNonNull(videoToolingService);
        this.videoMetadataService = Objects.requireNonNull(videoMetadataService);
        this.videoProcessingService = Objects.requireNonNull(videoProcessingService);

        setTitle("Upscaler Studio");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 880));
        setSize(settings.getWindowWidth(), settings.getWindowHeight());
        setLocationRelativeTo(null);

        JPanel root = new BackdropPanel(new BorderLayout(0, 14));
        root.setBackground(UiPalette.WINDOW);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(root);
        setJMenuBar(createMenuBar());

        root.add(createHeroHeader(), BorderLayout.NORTH);

        tabs.addTab("Image", workspacePanel);
        tabs.addTab("Video", videoWorkspacePanel);
        tabs.addTab("Settings", settingsPanel);
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createEmptyBorder());
        root.add(tabs, BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);

        installListeners();
        installFileDrop(root);
        loadModels();
        populateVideoEncoders(videoToolingService.listAvailableEncoders());
        syncSettingsToUi();
        refreshDevicesAsync();
        refreshToolingSummary();

        workspacePanel.setPreviewImages(null, null);
        workspacePanel.setSaveEnabled(false);
        workspacePanel.setPipelineText(buildSelectedImageScalePlan(settings.getScaleFactor()).describe());
        videoWorkspacePanel.setPlanText(buildVideoPlanText());
        detailsLabel.setText(buildDetailsText());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(10, 14, 18));
        menuBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JMenu fileMenu = new JMenu("File");
        JMenuItem openImageItem = new JMenuItem("Open Image...");
        openImageItem.addActionListener(e -> chooseInputImage());
        JMenuItem openVideoItem = new JMenuItem("Open Video...");
        openVideoItem.addActionListener(e -> chooseVideoFile());
        JMenuItem saveImageItem = new JMenuItem("Save Image Result As...");
        saveImageItem.addActionListener(e -> saveResultAs());
        JMenuItem chooseVideoOutputItem = new JMenuItem("Choose Video Output...");
        chooseVideoOutputItem.addActionListener(e -> chooseVideoOutputFile());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> handleWindowClosing());
        fileMenu.add(openImageItem);
        fileMenu.add(openVideoItem);
        fileMenu.addSeparator();
        fileMenu.add(saveImageItem);
        fileMenu.add(chooseVideoOutputItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem refreshDevicesItem = new JMenuItem("Refresh Compute Devices");
        refreshDevicesItem.addActionListener(e -> refreshDevicesAsync());
        JMenuItem refreshModelsItem = new JMenuItem("Reload Models");
        refreshModelsItem.addActionListener(e -> loadModels());
        JMenuItem openModelsItem = new JMenuItem("Open Models Folder");
        openModelsItem.addActionListener(e -> openFolder(AppDirectories.userModelsDirectory()));
        JMenuItem openDataItem = new JMenuItem("Open App Data");
        openDataItem.addActionListener(e -> openFolder(AppDirectories.appHome()));
        toolsMenu.add(refreshDevicesItem);
        toolsMenu.add(refreshModelsItem);
        toolsMenu.add(openModelsItem);
        toolsMenu.add(openDataItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem imageTabItem = new JMenuItem("Open Image Tab");
        imageTabItem.addActionListener(e -> tabs.setSelectedComponent(workspacePanel));
        JMenuItem videoTabItem = new JMenuItem("Open Video Tab");
        videoTabItem.addActionListener(e -> tabs.setSelectedComponent(videoWorkspacePanel));
        JMenuItem settingsTabItem = new JMenuItem("Open Settings Tab");
        settingsTabItem.addActionListener(e -> tabs.setSelectedComponent(settingsPanel));
        JMenuItem resetCompareItem = new JMenuItem("Reset Compare Divider");
        resetCompareItem.addActionListener(e -> workspacePanel.getPreviewPanel().resetDivider());
        viewMenu.add(imageTabItem);
        viewMenu.add(videoTabItem);
        viewMenu.add(settingsTabItem);
        viewMenu.addSeparator();
        viewMenu.add(resetCompareItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About Upscaler Studio");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JPanel createHeroHeader() {
        JPanel hero = new GlassPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(15, 22, 29, 120), getWidth(), getHeight(), new Color(11, 16, 22, 30)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g2.dispose();
            }
        };
        hero.setPreferredSize(new Dimension(0, 124));
        hero.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);
        left.add(new JLabel(loadLogoIcon()));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Upscaler Studio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f));
        JLabel subtitle = new JLabel("Image recovery, video upscale and motion synthesis in one dark studio shell.");
        subtitle.setForeground(UiPalette.MUTED);
        text.add(title);
        text.add(subtitle);
        left.add(text);
        hero.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        right.setOpaque(false);
        JLabel versionLabel = new JLabel("v" + UpscalerApplication.APP_VERSION + "  •  Real-ESRGAN + FFmpeg Motion Pipeline");
        versionLabel.setForeground(UiPalette.MUTED);
        styleHeroBadge(heroDeviceBadge, UiPalette.ACCENT_DARK, new Color(16, 120, 107, 70));
        styleHeroBadge(heroToolsBadge, UiPalette.WARM, new Color(197, 108, 64, 90));
        right.add(versionLabel);
        right.add(heroDeviceBadge);
        right.add(heroToolsBadge);
        hero.add(right, BorderLayout.EAST);
        return hero;
    }

    private void styleHeroBadge(JLabel badge, Color foreground, Color borderColor) {
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(new Color(255, 255, 255, 16));
        badge.setForeground(foreground);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new GlassPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        statusLabel.setForeground(UiPalette.TEXT);
        detailsLabel.setForeground(UiPalette.MUTED);
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(detailsLabel, BorderLayout.EAST);
        return statusBar;
    }

    private void installListeners() {
        workspacePanel.getOpenButton().addActionListener(e -> chooseInputImage());
        workspacePanel.getStartButton().addActionListener(e -> startImageUpscale());
        workspacePanel.getCancelButton().addActionListener(e -> cancelImageUpscale());
        workspacePanel.getSaveButton().addActionListener(e -> saveResultAs());

        workspacePanel.getScaleCombo().addActionListener(e -> {
            if (syncingUi) {
                return;
            }
            Integer scale = (Integer) workspacePanel.getScaleCombo().getSelectedItem();
            if (scale != null) {
                settings.setScaleFactor(scale);
                workspacePanel.setPipelineText(buildSelectedImageScalePlan(scale).describe());
                persistSettingsQuietly();
            }
        });

        workspacePanel.getModelCombo().addActionListener(e -> {
            if (syncingUi) {
                return;
            }
            ModelDefinition selected = (ModelDefinition) workspacePanel.getModelCombo().getSelectedItem();
            if (selected != null) {
                settings.setSelectedModel(selected.name());
                Integer scale = (Integer) workspacePanel.getScaleCombo().getSelectedItem();
                if (scale != null) {
                    workspacePanel.setPipelineText(ScalePlanner.plan(selected, scale).describe());
                }
                persistSettingsQuietly();
            }
        });

        videoWorkspacePanel.getOpenVideoButton().addActionListener(e -> chooseVideoFile());
        videoWorkspacePanel.getChooseOutputButton().addActionListener(e -> chooseVideoOutputFile());
        videoWorkspacePanel.getStartRenderButton().addActionListener(e -> startVideoRender());
        videoWorkspacePanel.getCancelRenderButton().addActionListener(e -> cancelVideoRender());
        videoWorkspacePanel.getPlayResultButton().addActionListener(e -> openFile(currentVideoOutputFile));
        videoWorkspacePanel.getRevealOutputButton().addActionListener(e -> revealFile(currentVideoOutputFile));

        videoWorkspacePanel.getModelCombo().addActionListener(e -> {
            if (!syncingUi) {
                videoWorkspacePanel.setPlanText(buildVideoPlanText());
                persistSettingsQuietly();
            }
        });

        videoWorkspacePanel.getScaleCombo().addActionListener(e -> {
            if (syncingUi) {
                return;
            }
            Integer scale = (Integer) videoWorkspacePanel.getScaleCombo().getSelectedItem();
            if (scale != null) {
                settings.setVideoScaleFactor(scale);
                videoWorkspacePanel.setPlanText(buildVideoPlanText());
                persistSettingsQuietly();
            }
        });

        videoWorkspacePanel.getFpsCombo().addActionListener(e -> {
            if (syncingUi) {
                return;
            }
            VideoFpsOption option = (VideoFpsOption) videoWorkspacePanel.getFpsCombo().getSelectedItem();
            if (option != null) {
                settings.setVideoTargetFrameRate(option.value());
                videoWorkspacePanel.setPlanText(buildVideoPlanText());
                persistSettingsQuietly();
            }
        });

        videoWorkspacePanel.getQualityCombo().addActionListener(e -> {
            if (syncingUi) {
                return;
            }
            VideoQualityPreset preset = (VideoQualityPreset) videoWorkspacePanel.getQualityCombo().getSelectedItem();
            if (preset != null) {
                settings.setVideoQualityPreset(preset.name());
                videoWorkspacePanel.setPlanText(buildVideoPlanText());
                persistSettingsQuietly();
            }
        });

        settingsPanel.getRefreshDevicesButton().addActionListener(e -> refreshDevicesAsync());
        settingsPanel.getReloadModelsButton().addActionListener(e -> loadModels());
        settingsPanel.getOpenModelsButton().addActionListener(e -> openFolder(AppDirectories.userModelsDirectory()));
        settingsPanel.getOpenAppDataButton().addActionListener(e -> openFolder(AppDirectories.appHome()));

        settingsPanel.getDeviceCombo().addActionListener(e -> {
            if (syncingUi) {
                return;
            }
            ComputeDevice device = (ComputeDevice) settingsPanel.getDeviceCombo().getSelectedItem();
            if (device != null) {
                settings.setDeviceSelection(device.id());
                updateSelectedDeviceUi(device);
                persistSettingsQuietly();
            }
        });

        settingsPanel.getTileSizeSpinner().addChangeListener(e -> {
            if (!syncingUi) {
                settings.setTileSize((Integer) settingsPanel.getTileSizeSpinner().getValue());
                persistSettingsQuietly();
            }
        });

        settingsPanel.getThreadProfileCombo().addActionListener(e -> {
            if (!syncingUi) {
                settings.setThreadProfile((String) settingsPanel.getThreadProfileCombo().getSelectedItem());
                persistSettingsQuietly();
            }
        });

        settingsPanel.getVideoEncoderCombo().addActionListener(e -> {
            if (syncingUi) {
                return;
            }
            VideoEncoderOption option = (VideoEncoderOption) settingsPanel.getVideoEncoderCombo().getSelectedItem();
            if (option != null) {
                settings.setVideoEncoderId(option.id());
                updateSelectedVideoEncoderUi(option);
                videoWorkspacePanel.setPlanText(buildVideoPlanText());
                persistSettingsQuietly();
            }
        });

        settingsPanel.getTtaCheckBox().addActionListener(e -> {
            if (!syncingUi) {
                settings.setTtaEnabled(settingsPanel.getTtaCheckBox().isSelected());
                persistSettingsQuietly();
            }
        });
    }

    private void syncSettingsToUi() {
        syncingUi = true;
        try {
            workspacePanel.getScaleCombo().setSelectedItem(settings.getScaleFactor());
            settingsPanel.getTileSizeSpinner().setValue(settings.getTileSize());
            settingsPanel.getThreadProfileCombo().setSelectedItem(settings.getThreadProfile());
            settingsPanel.getTtaCheckBox().setSelected(settings.isTtaEnabled());
            videoWorkspacePanel.getScaleCombo().setSelectedItem(settings.getVideoScaleFactor());
            videoWorkspacePanel.getQualityCombo().setSelectedItem(resolveVideoPreset(settings.getVideoQualityPreset()));
            selectVideoFpsOption(settings.getVideoTargetFrameRate());
            selectVideoEncoderOption(settings.getVideoEncoderId());
        } finally {
            syncingUi = false;
        }
    }

    private void loadModels() {
        List<ModelDefinition> models = modelRegistry.listModels();
        workspacePanel.setModels(models);
        videoWorkspacePanel.setModels(models);

        ModelDefinition selected = models.stream()
                .filter(model -> model.name().equals(settings.getSelectedModel()))
                .findFirst()
                .orElse(models.isEmpty() ? null : models.getFirst());

        if (selected != null) {
            syncingUi = true;
            try {
                workspacePanel.getModelCombo().setSelectedItem(selected);
                videoWorkspacePanel.getModelCombo().setSelectedItem(selected);
            } finally {
                syncingUi = false;
            }
        }
        refreshToolingSummary();
        workspacePanel.setPipelineText(buildSelectedImageScalePlan(settings.getScaleFactor()).describe());
        detailsLabel.setText(buildDetailsText());
    }

    private ScalePlan buildSelectedImageScalePlan(int targetScale) {
        ModelDefinition selected = (ModelDefinition) workspacePanel.getModelCombo().getSelectedItem();
        return ScalePlanner.plan(selected, targetScale);
    }

    private void refreshDevicesAsync() {
        settingsPanel.getRefreshDevicesButton().setEnabled(false);
        status("Scanning Vulkan devices and runtime adapters...");

        new SwingWorker<DeviceScanResult, Void>() {
            @Override
            protected DeviceScanResult doInBackground() {
                return deviceDetectionService.detectDevices();
            }

            @Override
            protected void done() {
                settingsPanel.getRefreshDevicesButton().setEnabled(true);
                try {
                    DeviceScanResult result = get();
                    populateDevices(result.devices());
                    settingsPanel.setDiagnosticsText(buildDiagnosticsText(result));
                    status(result.summary());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status("Device scan interrupted.");
                } catch (ExecutionException e) {
                    showError("Failed to detect compute devices.", e.getCause());
                }
            }
        }.execute();
    }

    private void populateDevices(List<ComputeDevice> devices) {
        syncingUi = true;
        try {
            settingsPanel.getDeviceCombo().removeAllItems();
            for (ComputeDevice device : devices) {
                settingsPanel.getDeviceCombo().addItem(device);
            }
            Optional<ComputeDevice> selected = devices.stream()
                    .filter(device -> device.id().equals(settings.getDeviceSelection()))
                    .findFirst();
            ComputeDevice device = selected.orElse(devices.getFirst());
            settingsPanel.getDeviceCombo().setSelectedItem(device);
            settings.setDeviceSelection(device.id());
            updateSelectedDeviceUi(device);
        } finally {
            syncingUi = false;
        }
        persistSettingsQuietly();
    }

    private void populateVideoEncoders(List<VideoEncoderOption> encoders) {
        syncingUi = true;
        try {
            settingsPanel.setVideoEncoders(encoders);
            selectVideoEncoderOption(settings.getVideoEncoderId());
        } finally {
            syncingUi = false;
        }
    }

    private void selectVideoEncoderOption(String encoderId) {
        for (int index = 0; index < settingsPanel.getVideoEncoderCombo().getItemCount(); index++) {
            VideoEncoderOption option = settingsPanel.getVideoEncoderCombo().getItemAt(index);
            if (option.id().equalsIgnoreCase(encoderId)) {
                settingsPanel.getVideoEncoderCombo().setSelectedIndex(index);
                updateSelectedVideoEncoderUi(option);
                return;
            }
        }
        if (settingsPanel.getVideoEncoderCombo().getItemCount() > 0) {
            VideoEncoderOption fallback = settingsPanel.getVideoEncoderCombo().getItemAt(0);
            settingsPanel.getVideoEncoderCombo().setSelectedItem(fallback);
            settings.setVideoEncoderId(fallback.id());
            updateSelectedVideoEncoderUi(fallback);
        }
    }

    private void chooseInputImage() {
        JFileChooser chooser = new JFileChooser(resolveDirectory(settings.lastInputDirectoryPath()).toFile());
        chooser.setDialogTitle("Open Image");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "webp", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openImage(chooser.getSelectedFile().toPath());
        }
    }

    private void openImage(Path file) {
        try {
            BufferedImage image = ImageIO.read(file.toFile());
            if (image == null) {
                throw new IOException("The selected file is not a readable image.");
            }
            currentInputFile = file;
            inputImage = image;
            outputImage = null;
            workspacePanel.setFile(file, image.getWidth(), image.getHeight());
            workspacePanel.setPreviewImages(inputImage, null);
            workspacePanel.setResultMetaText("Input ready • " + image.getWidth() + " × " + image.getHeight() + " px");
            workspacePanel.setSaveEnabled(false);
            settings.setLastInputDirectory(file.getParent() != null ? file.getParent().toString() : "");
            persistSettingsQuietly();
            status("Loaded image " + file.getFileName());
            tabs.setSelectedComponent(workspacePanel);
        } catch (IOException e) {
            showError("Failed to open image.", e);
        }
    }

    private void startImageUpscale() {
        if (isAnotherJobRunning(currentImageWorker)) {
            return;
        }
        if (currentInputFile == null || inputImage == null) {
            JOptionPane.showMessageDialog(this, "Open an image first.", "No image", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ModelDefinition model = (ModelDefinition) workspacePanel.getModelCombo().getSelectedItem();
        Integer targetScale = (Integer) workspacePanel.getScaleCombo().getSelectedItem();
        ComputeDevice device = (ComputeDevice) settingsPanel.getDeviceCombo().getSelectedItem();
        if (model == null || targetScale == null) {
            return;
        }

        ScalePlan scalePlan = ScalePlanner.plan(model, targetScale);
        workspacePanel.setPipelineText(scalePlan.describe());
        UpscaleRequest request = new UpscaleRequest(
                currentInputFile,
                model,
                scalePlan,
                device == null || device.isAuto() ? null : device.engineGpuId(),
                (Integer) settingsPanel.getTileSizeSpinner().getValue(),
                Objects.toString(settingsPanel.getThreadProfileCombo().getSelectedItem(), "1:2:2"),
                settingsPanel.getTtaCheckBox().isSelected()
        );

        settings.setSelectedModel(model.name());
        settings.setScaleFactor(targetScale);
        persistSettingsQuietly();

        workspacePanel.clearLog();
        workspacePanel.setBusy(true);
        workspacePanel.setSaveEnabled(false);
        workspacePanel.resetProgress();
        workspacePanel.appendLog("Preparing request: model=" + model.name() + ", scale plan=" + scalePlan.describe());
        status("Preparing image runtime...");
        activeImagePass = 1;
        totalImagePasses = scalePlan.passes().size();

        currentImageWorker = new SwingWorker<>() {
            @Override
            protected UpscaleResult doInBackground() throws Exception {
                return upscaleService.upscale(request, new UpscaleProgressListener() {
                    @Override
                    public void onStatus(String message, int currentPass, int totalPasses) {
                        publish(IMAGE_STATUS_PREFIX + currentPass + "|" + totalPasses + "|" + message);
                    }

                    @Override
                    public void onLog(String line) {
                        publish(line);
                    }
                });
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    if (chunk.startsWith(IMAGE_STATUS_PREFIX)) {
                        String payload = chunk.substring(IMAGE_STATUS_PREFIX.length());
                        String[] parts = payload.split("\\|", 3);
                        if (parts.length == 3) {
                            activeImagePass = Integer.parseInt(parts[0]);
                            totalImagePasses = Integer.parseInt(parts[1]);
                            String message = parts[2];
                            workspacePanel.setStatusText(message);
                            statusLabel.setText(message);
                            int statusProgress = Math.round(((activeImagePass - 1) * 100f) / Math.max(1, totalImagePasses));
                            if (message.startsWith("Post-resizing")) {
                                statusProgress = 96;
                            } else if ("Upscale complete".equals(message)) {
                                statusProgress = 100;
                            }
                            workspacePanel.setProgressValue(statusProgress);
                        }
                    } else if (chunk.matches("\\d+(\\.\\d+)?%")) {
                        double passPercent = Double.parseDouble(chunk.replace("%", ""));
                        int overall = (int) Math.round((((activeImagePass - 1) + (passPercent / 100.0)) / Math.max(1, totalImagePasses)) * 100);
                        workspacePanel.setProgressValue(overall);
                    } else {
                        workspacePanel.appendLog(chunk);
                    }
                }
            }

            @Override
            protected void done() {
                workspacePanel.setBusy(false);
                currentImageWorker = null;
                try {
                    UpscaleResult result = get();
                    outputImage = result.image();
                    workspacePanel.setPreviewImages(inputImage, outputImage);
                    workspacePanel.setResultMetaText("Upscaled result • " + outputImage.getWidth() + " × " + outputImage.getHeight() + " px");
                    workspacePanel.setProgressValue(100);
                    workspacePanel.setSaveEnabled(true);
                    status("Image upscale complete.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof UpscaleCancelledException) {
                        workspacePanel.setStatusText("Cancelled");
                        status("Image upscale cancelled.");
                    } else {
                        workspacePanel.setStatusText("Failed");
                        showError("Image upscale failed.", cause);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    workspacePanel.setStatusText("Interrupted");
                    status("Image worker interrupted.");
                }
            }
        };
        currentImageWorker.execute();
    }

    private void cancelImageUpscale() {
        if (currentImageWorker == null) {
            return;
        }
        status("Cancelling image upscale...");
        workspacePanel.setStatusText("Cancelling...");
        upscaleService.cancelCurrentJob();
    }

    private void chooseVideoFile() {
        JFileChooser chooser = new JFileChooser(resolveDirectory(settings.lastVideoDirectoryPath()).toFile());
        chooser.setDialogTitle("Open Video");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Videos", "mp4", "mov", "mkv", "webm", "avi", "m4v"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openVideo(chooser.getSelectedFile().toPath());
        }
    }

    private void openVideo(Path file) {
        if (isAnotherJobRunning(currentVideoWorker)) {
            return;
        }
        videoWorkspacePanel.clearLog();
        videoWorkspacePanel.appendLog("Probing metadata for " + file.getFileName());
        status("Inspecting video metadata...");

        new SwingWorker<VideoMetadata, Void>() {
            private BufferedImage thumbnail;

            @Override
            protected VideoMetadata doInBackground() throws Exception {
                VideoMetadata metadata = videoMetadataService.probe(file);
                try {
                    thumbnail = videoMetadataService.extractThumbnail(metadata);
                } catch (IOException ignored) {
                    thumbnail = null;
                }
                return metadata;
            }

            @Override
            protected void done() {
                try {
                    currentVideoFile = file;
                    currentVideoMetadata = get();
                    currentVideoThumbnail = thumbnail;
                    videoWorkspacePanel.setInputFile(file);
                    videoWorkspacePanel.setMetadata(currentVideoMetadata);
                    videoWorkspacePanel.setPoster(currentVideoThumbnail, currentVideoMetadata);
                    settings.setLastVideoDirectory(file.getParent() != null ? file.getParent().toString() : "");
                    persistSettingsQuietly();
                    if (currentVideoOutputFile == null || !currentVideoOutputFile.getFileName().toString().startsWith(stripExtension(file.getFileName().toString()))) {
                        currentVideoOutputFile = defaultVideoOutputFor(file);
                        videoWorkspacePanel.setOutputFile(currentVideoOutputFile);
                    }
                    videoWorkspacePanel.setResultActionsEnabled(currentVideoOutputFile != null && Files.exists(currentVideoOutputFile));
                    videoWorkspacePanel.setPlanText(buildVideoPlanText());
                    status("Loaded video " + file.getFileName());
                    tabs.setSelectedComponent(videoWorkspacePanel);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    showError("Failed to inspect video.", e.getCause());
                }
            }
        }.execute();
    }

    private void chooseVideoOutputFile() {
        JFileChooser chooser = new JFileChooser(resolveDirectory(settings.lastVideoDirectoryPath()).toFile());
        chooser.setDialogTitle("Choose Video Output");
        chooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter mp4Filter = new FileNameExtensionFilter("MP4 video", "mp4");
        FileNameExtensionFilter mkvFilter = new FileNameExtensionFilter("MKV video", "mkv");
        chooser.addChoosableFileFilter(mp4Filter);
        chooser.addChoosableFileFilter(mkvFilter);
        chooser.setFileFilter(mp4Filter);
        chooser.setSelectedFile((currentVideoOutputFile != null ? currentVideoOutputFile : Path.of("upscaled-video.mp4")).toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        String extension = chooser.getFileFilter().getDescription().startsWith("MKV") ? "mkv" : "mp4";
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith("." + extension)) {
            file = new File(file.getParentFile(), file.getName() + "." + extension);
        }
        currentVideoOutputFile = file.toPath();
        videoWorkspacePanel.setOutputFile(currentVideoOutputFile);
        videoWorkspacePanel.setResultActionsEnabled(Files.exists(currentVideoOutputFile));
    }

    private void startVideoRender() {
        if (isAnotherJobRunning(currentVideoWorker)) {
            return;
        }
        if (currentVideoFile == null || currentVideoMetadata == null) {
            JOptionPane.showMessageDialog(this, "Open a video first.", "No video", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentVideoOutputFile == null) {
            chooseVideoOutputFile();
            if (currentVideoOutputFile == null) {
                return;
            }
        }

        ModelDefinition model = (ModelDefinition) videoWorkspacePanel.getModelCombo().getSelectedItem();
        Integer scale = (Integer) videoWorkspacePanel.getScaleCombo().getSelectedItem();
        VideoFpsOption fpsOption = (VideoFpsOption) videoWorkspacePanel.getFpsCombo().getSelectedItem();
        VideoQualityPreset qualityPreset = (VideoQualityPreset) videoWorkspacePanel.getQualityCombo().getSelectedItem();
        VideoEncoderOption encoder = (VideoEncoderOption) settingsPanel.getVideoEncoderCombo().getSelectedItem();
        ComputeDevice device = (ComputeDevice) settingsPanel.getDeviceCombo().getSelectedItem();
        if (model == null || scale == null || fpsOption == null || qualityPreset == null || encoder == null) {
            return;
        }
        if (scale == 1 && fpsOption.value() == 0) {
            JOptionPane.showMessageDialog(this, "Choose either a scale factor above 1x or a target frame rate above Original.", "Nothing to render", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        settings.setSelectedModel(model.name());
        settings.setVideoScaleFactor(scale);
        settings.setVideoTargetFrameRate(fpsOption.value());
        settings.setVideoQualityPreset(qualityPreset.name());
        settings.setVideoEncoderId(encoder.id());
        persistSettingsQuietly();

        ScalePlan scalePlan = scale <= 1 ? ScalePlanner.plan(1, model.nativeScale()) : ScalePlanner.plan(model, scale);
        VideoUpscaleRequest request = new VideoUpscaleRequest(
                currentVideoFile,
                currentVideoOutputFile,
                model,
                scale,
                scalePlan,
                fpsOption.value(),
                qualityPreset,
                encoder.id(),
                device == null || device.isAuto() ? null : device.engineGpuId(),
                (Integer) settingsPanel.getTileSizeSpinner().getValue(),
                Objects.toString(settingsPanel.getThreadProfileCombo().getSelectedItem(), "1:2:2"),
                settingsPanel.getTtaCheckBox().isSelected()
        );

        videoWorkspacePanel.clearLog();
        videoWorkspacePanel.resetProgress();
        videoWorkspacePanel.setBusy(true);
        videoWorkspacePanel.setResultText("Rendering in progress...");
        videoWorkspacePanel.setPlanText(buildVideoPlanText());
        status("Starting video render...");

        currentVideoWorker = new SwingWorker<>() {
            @Override
            protected VideoUpscaleResult doInBackground() throws Exception {
                return videoProcessingService.process(request, new VideoProgressListener() {
                    @Override
                    public void onStage(String stage, int overallPercent) {
                        publish(VIDEO_STAGE_PREFIX + overallPercent + "|" + stage);
                    }

                    @Override
                    public void onLog(String line) {
                        publish(line);
                    }
                });
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    if (chunk.startsWith(VIDEO_STAGE_PREFIX)) {
                        String payload = chunk.substring(VIDEO_STAGE_PREFIX.length());
                        String[] parts = payload.split("\\|", 2);
                        if (parts.length == 2) {
                            int percent = Integer.parseInt(parts[0]);
                            String stage = parts[1];
                            videoWorkspacePanel.setProgress(percent, stage);
                            status(stage);
                        }
                    } else {
                        videoWorkspacePanel.appendLog(chunk);
                    }
                }
            }

            @Override
            protected void done() {
                videoWorkspacePanel.setBusy(false);
                currentVideoWorker = null;
                try {
                    VideoUpscaleResult result = get();
                    videoWorkspacePanel.setProgress(100, "Render complete");
                    videoWorkspacePanel.setResultText("Rendered " + result.outputWidth() + " × " + result.outputHeight() + " at " + result.outputFrameRate() + " fps");
                    videoWorkspacePanel.setResultActionsEnabled(Files.exists(result.outputFile()));
                    status("Video render complete.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof UpscaleCancelledException) {
                        videoWorkspacePanel.setProgress(0, "Cancelled");
                        videoWorkspacePanel.setResultText("Video render cancelled");
                        status("Video render cancelled.");
                    } else {
                        videoWorkspacePanel.setProgress(0, "Failed");
                        videoWorkspacePanel.setResultText("Video render failed");
                        showError("Video render failed.", cause);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    videoWorkspacePanel.setProgress(0, "Interrupted");
                    status("Video worker interrupted.");
                }
            }
        };
        currentVideoWorker.execute();
    }

    private void cancelVideoRender() {
        if (currentVideoWorker == null) {
            return;
        }
        status("Cancelling video render...");
        videoWorkspacePanel.setProgress(0, "Cancelling...");
        videoProcessingService.cancelCurrentJob();
    }

    private void saveResultAs() {
        if (outputImage == null) {
            JOptionPane.showMessageDialog(this, "There is no image result to save yet.", "No result", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser(resolveDirectory(settings.lastOutputDirectoryPath()).toFile());
        chooser.setDialogTitle("Save Image Result As");
        chooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG image", "png");
        FileNameExtensionFilter jpegFilter = new FileNameExtensionFilter("JPEG image", "jpg", "jpeg");
        chooser.addChoosableFileFilter(pngFilter);
        chooser.addChoosableFileFilter(jpegFilter);
        chooser.setFileFilter(pngFilter);
        chooser.setSelectedFile(new File("upscaled-result.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        String format = chooser.getFileFilter().getDescription().startsWith("JPEG") ? "jpg" : "png";
        String filename = file.getName().toLowerCase(Locale.ROOT);
        if (!filename.endsWith("." + format) && !(format.equals("jpg") && filename.endsWith(".jpeg"))) {
            file = new File(file.getParentFile(), file.getName() + "." + format);
        }

        try {
            BufferedImage imageToWrite = outputImage;
            if ("jpg".equals(format) && outputImage.getColorModel().hasAlpha()) {
                BufferedImage converted = new BufferedImage(outputImage.getWidth(), outputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = converted.createGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, converted.getWidth(), converted.getHeight());
                g2.drawImage(outputImage, 0, 0, null);
                g2.dispose();
                imageToWrite = converted;
            }
            ImageIO.write(imageToWrite, format, file);
            settings.setLastOutputDirectory(file.getParentFile() != null ? file.getParentFile().getAbsolutePath() : "");
            persistSettingsQuietly();
            status("Saved image result to " + file.getName());
        } catch (IOException e) {
            showError("Failed to save image result.", e);
        }
    }

    private void installFileDrop(JComponent component) {
        TransferHandler handler = new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        Path path = files.getFirst().toPath();
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        if (name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".mkv") || name.endsWith(".webm") || name.endsWith(".avi") || name.endsWith(".m4v")) {
                            openVideo(path);
                            tabs.setSelectedComponent(videoWorkspacePanel);
                        } else {
                            openImage(path);
                            tabs.setSelectedComponent(workspacePanel);
                        }
                        return true;
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    showError("Failed to import dropped file.", e);
                }
                return false;
            }
        };
        component.setTransferHandler(handler);
        workspacePanel.setTransferHandler(handler);
        workspacePanel.getPreviewPanel().setTransferHandler(handler);
        videoWorkspacePanel.setTransferHandler(handler);
    }

    private void openFolder(Path path) {
        try {
            Files.createDirectories(path);
            if (!Desktop.isDesktopSupported()) {
                throw new IOException("Desktop integration is not available in this environment.");
            }
            Desktop.getDesktop().open(path.toFile());
        } catch (IOException e) {
            showError("Failed to open folder.", e);
        }
    }

    private void openFile(Path path) {
        if (path == null || Files.notExists(path)) {
            JOptionPane.showMessageDialog(this, "The result file does not exist yet.", "No file", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new IOException("Desktop integration is not available in this environment.");
            }
            Desktop.getDesktop().open(path.toFile());
        } catch (IOException e) {
            showError("Failed to open file.", e);
        }
    }

    private void revealFile(Path path) {
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Choose an output file first.", "No output", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Path directory = path.toAbsolutePath().getParent();
        if (directory == null) {
            directory = Path.of(".");
        }
        openFolder(directory);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                "Upscaler Studio\n" +
                        "Version " + UpscalerApplication.APP_VERSION + "\n\n" +
                        "Image pipeline: bundled Real-ESRGAN NCNN Vulkan\n" +
                        "Video pipeline: FFmpeg extraction/encode + optional motion interpolation\n" +
                        "Exact image and video scales: 1x/2x/3x/4x/6x/8x\n" +
                        "Target video FPS: Original / 60 / 120 / 240\n\n" +
                        "Video rendering requires ffmpeg and ffprobe in PATH.",
                "About Upscaler Studio",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleWindowClosing() {
        if (currentImageWorker != null || currentVideoWorker != null) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "A job is still running. Cancel it and exit?",
                    "Exit Upscaler Studio",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            if (currentImageWorker != null) {
                cancelImageUpscale();
            }
            if (currentVideoWorker != null) {
                cancelVideoRender();
            }
        }

        settings.setWindowWidth(getWidth());
        settings.setWindowHeight(getHeight());
        persistSettingsQuietly();
        dispose();
    }

    private boolean isAnotherJobRunning(SwingWorker<?, ?> sameWorker) {
        if ((currentImageWorker != null && currentImageWorker != sameWorker) || (currentVideoWorker != null && currentVideoWorker != sameWorker)) {
            JOptionPane.showMessageDialog(this, "Another job is already running. Finish or cancel it first.", "Busy", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        return false;
    }

    private Path resolveDirectory(Path preferred) {
        if (preferred != null && Files.exists(preferred)) {
            return preferred;
        }
        return Path.of(System.getProperty("user.home", "."));
    }

    private Path defaultVideoOutputFor(Path inputFile) {
        String extension = "mp4";
        String baseName = stripExtension(inputFile.getFileName().toString());
        return inputFile.getParent().resolve(baseName + "-studio." + extension);
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    private void updateSelectedDeviceUi(ComputeDevice device) {
        heroDeviceBadge.setText(device.displayName());
        workspacePanel.setSelectedDeviceText(device.displayName());
        settingsPanel.setDeviceDetailsText(device.description());
    }

    private void refreshToolingSummary() {
        populateVideoEncoders(videoToolingService.listAvailableEncoders());
        heroToolsBadge.setText(videoToolingService.buildEnvironmentSummary());
        detailsLabel.setText(buildDetailsText());
    }

    private String buildDetailsText() {
        return "Platform: " + PlatformDetector.detect().resourceFolder() + "  •  models: " + modelRegistry.listModels().size() + "  •  " + videoToolingService.buildEnvironmentSummary();
    }

    private String buildDiagnosticsText(DeviceScanResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("Detection summary: ").append(result.summary()).append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("Image scales: x2, x3, x4, x6, x8").append(System.lineSeparator());
        builder.append("Video scales: 1x, x2, x3, x4, x6, x8").append(System.lineSeparator());
        builder.append("Safe outscale mode: bundled x4 models render at native 4x and exact output scale is produced via final resize.")
                .append(System.lineSeparator());
        builder.append("This avoids direct NCNN x2/x3 model calls that can create tile-like block artifacts on some systems.")
                .append(System.lineSeparator());
        builder.append("Video FPS targets: Original, 60, 120, 240").append(System.lineSeparator());
        builder.append(videoToolingService.buildEnvironmentSummary()).append(System.lineSeparator());
        builder.append("Current platform: ").append(PlatformDetector.detect().resourceFolder()).append(System.lineSeparator());
        builder.append("Bundled runtime is extracted to: ").append(AppDirectories.runtimeDirectory()).append(System.lineSeparator());
        builder.append("Custom models directory: ").append(AppDirectories.userModelsDirectory()).append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("Detected selectable devices:").append(System.lineSeparator());
        for (ComputeDevice device : result.devices()) {
            builder.append("- ").append(device.displayName()).append(" :: ").append(device.description()).append(System.lineSeparator());
        }
        builder.append(System.lineSeparator()).append("Detected video encoders:").append(System.lineSeparator());
        for (int index = 0; index < settingsPanel.getVideoEncoderCombo().getItemCount(); index++) {
            VideoEncoderOption option = settingsPanel.getVideoEncoderCombo().getItemAt(index);
            builder.append("- ").append(option.displayName()).append(" :: ").append(option.description()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private void selectVideoFpsOption(int value) {
        syncingUi = true;
        try {
            for (int i = 0; i < videoWorkspacePanel.getFpsCombo().getItemCount(); i++) {
                VideoFpsOption option = videoWorkspacePanel.getFpsCombo().getItemAt(i);
                if (option.value() == value) {
                    videoWorkspacePanel.getFpsCombo().setSelectedIndex(i);
                    return;
                }
            }
            videoWorkspacePanel.getFpsCombo().setSelectedIndex(0);
        } finally {
            syncingUi = false;
        }
    }

    private VideoQualityPreset resolveVideoPreset(String name) {
        try {
            return VideoQualityPreset.valueOf(name);
        } catch (Exception ignored) {
            return VideoQualityPreset.BALANCED;
        }
    }

    private String buildVideoPlanText() {
        Integer scale = (Integer) videoWorkspacePanel.getScaleCombo().getSelectedItem();
        if (scale == null) {
            scale = settings.getVideoScaleFactor();
        }
        VideoFpsOption fpsOption = (VideoFpsOption) videoWorkspacePanel.getFpsCombo().getSelectedItem();
        if (fpsOption == null) {
            fpsOption = new VideoFpsOption(settings.getVideoTargetFrameRate(), settings.getVideoTargetFrameRate() == 0 ? "Original FPS" : settings.getVideoTargetFrameRate() + " fps");
        }
        VideoQualityPreset preset = (VideoQualityPreset) videoWorkspacePanel.getQualityCombo().getSelectedItem();
        if (preset == null) {
            preset = resolveVideoPreset(settings.getVideoQualityPreset());
        }
        ModelDefinition model = (ModelDefinition) videoWorkspacePanel.getModelCombo().getSelectedItem();
        ScalePlan scalePlan = scale <= 1 ? ScalePlanner.plan(1, model == null ? 4 : model.nativeScale()) : ScalePlanner.plan(model, scale);
        VideoEncoderOption encoder = (VideoEncoderOption) settingsPanel.getVideoEncoderCombo().getSelectedItem();
        if (encoder == null && settingsPanel.getVideoEncoderCombo().getItemCount() > 0) {
            encoder = settingsPanel.getVideoEncoderCombo().getItemAt(0);
        }

        String scaleText = scale <= 1 ? "keep source resolution" : scalePlan.describe();
        String fpsText;
        if (fpsOption.value() <= 0) {
            fpsText = "keep source fps";
        } else if (currentVideoMetadata != null && fpsOption.value() > currentVideoMetadata.frameRate()) {
            fpsText = "motion interpolation to " + fpsOption.value() + " fps";
        } else {
            fpsText = "export at " + fpsOption.value() + " fps";
        }
        String encoderText = encoder == null ? "Auto encode" : encoder.displayName();
        return scaleText + " • " + fpsText + " • " + preset.displayName() + " • " + encoderText;
    }

    private void updateSelectedVideoEncoderUi(VideoEncoderOption option) {
        settingsPanel.setVideoEncoderDetailsText(option.description());
    }

    private void persistSettingsQuietly() {
        try {
            settingsRepository.save(settings);
        } catch (IOException ignored) {
            status("Warning: failed to persist settings.");
        }
    }

    private void status(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message, Throwable throwable) {
        status(message);
        workspacePanel.appendLog("ERROR: " + throwable.getMessage());
        videoWorkspacePanel.appendLog("ERROR: " + throwable.getMessage());
        JOptionPane.showMessageDialog(
                this,
                message + "\n\n" + throwable.getMessage(),
                "Upscaler Studio",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private ImageIcon loadLogoIcon() {
        java.net.URL resource = MainFrame.class.getResource("/assets/logo.png");
        if (resource == null) {
            return new ImageIcon(new BufferedImage(52, 52, BufferedImage.TYPE_INT_ARGB));
        }
        ImageIcon icon = new ImageIcon(resource);
        Image scaled = icon.getImage().getScaledInstance(54, 54, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
