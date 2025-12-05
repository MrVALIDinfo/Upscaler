package ui;

import ImageScaler.ImageUpscaler;
import ImageScaler.InputImage;
import ImageScaler.OutputImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class ImageSectionPanel extends JPanel {

    private BufferedImage currentImage;
    private final JLabel previewLabel = new JLabel("Drop an image or click \"Select Image\"", SwingConstants.CENTER);
    private final JLabel previewInfoLabel = new JLabel("No image loaded");
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton saveImageBtn = createStyledButton("Save Image", false);
    private double zoom = 1.0;
    private int scaleMultiplier = 4;

    // ═══════════════════════════════════════════════════════════════════════════
    // NEW: Custom preview panel with hover zoom
    // ═══════════════════════════════════════════════════════════════════════════
    private HoverZoomPreviewPanel hoverZoomPanel;

    // ═══════════════════════════════════════════════════════════════════════════
    // Color Palette (matching target design)
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Color COLOR_BG_MAIN = new Color(15, 23, 42);           // #0F172A
    private static final Color COLOR_CARD = new Color(39, 46, 63);              // #272E3F
    private static final Color COLOR_INPUT = new Color(59, 66, 84);             // #3B4254
    private static final Color COLOR_TEXT_PRIMARY = new Color(229, 231, 235);   // #E5E7EB
    private static final Color COLOR_TEXT_MUTED = new Color(156, 163, 175);     // #9CA3AF
    private static final Color COLOR_TEXT_TITLE = new Color(249, 250, 251);     // #F9FAFB
    private static final Color COLOR_BUTTON_PRIMARY_BG = new Color(229, 231, 235); // #E5E7EB
    private static final Color COLOR_BUTTON_PRIMARY_TEXT = new Color(17, 24, 39);  // #111827
    private static final Color COLOR_TRACK = new Color(211, 212, 216);          // #D3D4D8
    private static final Color COLOR_PREVIEW_BG = new Color(31, 41, 55);        // Preview area background

    // Legacy color references (kept for compatibility)
    private static final Color BG_MAIN = COLOR_BG_MAIN;
    private static final Color BG_LEFT = COLOR_CARD;
    private static final Color BG_CARD = COLOR_CARD;
    private static final Color ACCENT = COLOR_BUTTON_PRIMARY_BG;
    private static final Color TEXT_PRIMARY = COLOR_TEXT_PRIMARY;
    private static final Color TEXT_MUTED = COLOR_TEXT_MUTED;

    private static final int CARD_RADIUS = 20;
    private static final int LEFT_PANEL_WIDTH = 350;

    public ImageSectionPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(COLOR_BG_MAIN);

        // ==== Левая колонка: контролы ====
        JPanel leftCard = createRoundedCardPanel();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
        leftCard.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, 0));
        leftCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title
        leftCard.add(createTitleLabel("Upscayl Image"));
        leftCard.add(Box.createVerticalStrut(24));

        // ─── Block: Image ───
        leftCard.add(createSectionLabel("Image"));
        leftCard.add(Box.createVerticalStrut(8));

        JPanel imageRow = createFileChooserRow();
        leftCard.add(imageRow);
        leftCard.add(Box.createVerticalStrut(20));

        // ─── Block: Model ───
        leftCard.add(createSectionLabel("Model"));
        leftCard.add(Box.createVerticalStrut(8));
        JComboBox<String> modelBox = new JComboBox<>(new String[]{
                "realesrgan-x4plus",
                "realesrgan-x4plus-anime"
        });
        styleComboBox(modelBox);
        leftCard.add(wrapComboBox(modelBox));
        leftCard.add(Box.createVerticalStrut(20));

        // ─── Block: Scale ───
        leftCard.add(createSectionLabel("Scale"));
        leftCard.add(Box.createVerticalStrut(8));
        JLabel scaleLabel = new JLabel("Upscale: x4");
        scaleLabel.setForeground(TEXT_MUTED);
        scaleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        scaleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCard.add(scaleLabel);
        leftCard.add(Box.createVerticalStrut(8));

        JSlider scaleSlider = createStyledSlider(2, 8, 4);
        scaleSlider.addChangeListener(e -> {
            scaleMultiplier = scaleSlider.getValue();
            scaleLabel.setText("Upscale: x" + scaleMultiplier);
        });
        leftCard.add(scaleSlider);
        leftCard.add(Box.createVerticalStrut(4));
        leftCard.add(createScaleLabelsPanel());
        leftCard.add(Box.createVerticalStrut(20));

        // ─── Block: Start Upscaling ───
        leftCard.add(createSectionLabel("Process"));
        leftCard.add(Box.createVerticalStrut(8));
        JButton upscaleBtn = createStyledButton("Start upscaling", true);
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
                progressBar.setString("Upscaling…");

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        File in = input;
                        File out = output;

                        int repeats = getRepeatCount(scaleMultiplier);
                        System.out.println("[UI] Target scale: x" + scaleMultiplier +
                                ", repeats: " + repeats);

                        for (int i = 0; i < repeats; i++) {
                            System.out.println("[UI] Upscale round " + (i + 1) + "/" + repeats);
                            ImageUpscaler.upscaleImage(
                                    in.getAbsolutePath(),
                                    out.getAbsolutePath(),
                                    modelBox.getSelectedItem().toString()
                            );

                            if (i < repeats - 1) {
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
                            get();

                            if (currentImage != null) {
                                zoom = 1.0;
                                updatePreview();
                                previewInfoLabel.setText(currentImage.getWidth() + " × " + currentImage.getHeight() + " px • Upscaled");
                            }

                            progressBar.setIndeterminate(false);
                            progressBar.setString("Finished");
                            saveImageBtn.setEnabled(true);

                        } catch (ExecutionException ex) {
                            Throwable cause = ex.getCause();
                            if (cause != null) {
                                cause.printStackTrace();
                            } else {
                                ex.printStackTrace();
                            }

                            progressBar.setIndeterminate(false);
                            progressBar.setString("Failed");

                            JOptionPane.showMessageDialog(
                                    ImageSectionPanel.this,
                                    "❌ Upscale failed:\n" +
                                            (cause != null ? cause.getMessage() : ex.getMessage()),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            progressBar.setIndeterminate(false);
                            progressBar.setString("Interrupted");
                        }
                    }
                }.execute();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "⚠ Error during processing:\n" + ex.getMessage());
            }
        });
        leftCard.add(upscaleBtn);
        leftCard.add(Box.createVerticalStrut(12));

        // ─── Block: Save ───
        saveImageBtn.setEnabled(false);
        saveImageBtn.addActionListener(e -> {
            if (currentImage != null) {
                OutputImage.saveImage(currentImage);
            }
        });
        leftCard.add(saveImageBtn);
        leftCard.add(Box.createVerticalStrut(16));

        // Прогрессбар
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setBackground(COLOR_INPUT);
        progressBar.setForeground(COLOR_BUTTON_PRIMARY_BG);
        leftCard.add(progressBar);

        leftCard.add(Box.createVerticalGlue());

        // Wrapper for left card
        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(leftCard, BorderLayout.CENTER);

        // ==== Правая часть: превью ====
        JPanel rightCard = createRoundedCardPanel();
        rightCard.setLayout(new BorderLayout());
        rightCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ═══════════════════════════════════════════════════════════════════════════
        // CHANGED: Simplified top bar - removed zoom buttons
        // ═══════════════════════════════════════════════════════════════════════════
        JPanel topPreviewBar = new JPanel(new BorderLayout());
        topPreviewBar.setOpaque(false);
        topPreviewBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        previewInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        previewInfoLabel.setForeground(TEXT_MUTED);
        topPreviewBar.add(previewInfoLabel, BorderLayout.WEST);

        // ═══════════════════════════════════════════════════════════════════════════
        // NEW: Hint label for hover zoom
        // ═══════════════════════════════════════════════════════════════════════════
        JLabel hintLabel = new JLabel("Hover to zoom");
        hintLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hintLabel.setForeground(COLOR_TEXT_MUTED);
        topPreviewBar.add(hintLabel, BorderLayout.EAST);

        rightCard.add(topPreviewBar, BorderLayout.NORTH);

        // ═══════════════════════════════════════════════════════════════════════════
        // CHANGED: Use custom HoverZoomPreviewPanel instead of scroll pane with label
        // ═══════════════════════════════════════════════════════════════════════════
        hoverZoomPanel = new HoverZoomPreviewPanel();
        rightCard.add(hoverZoomPanel, BorderLayout.CENTER);

        add(leftWrapper, BorderLayout.WEST);
        add(rightCard, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NEW: Custom panel with hover-based zoom functionality
    // ═══════════════════════════════════════════════════════════════════════════
    private class HoverZoomPreviewPanel extends JPanel {
        private boolean isHovering = false;
        private int mouseX = 0;
        private int mouseY = 0;
        private static final double HOVER_ZOOM_FACTOR = 2.0;

        public HoverZoomPreviewPanel() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (currentImage != null) {
                        isHovering = true;
                        mouseX = e.getX();
                        mouseY = e.getY();
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovering = false;
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                    if (isHovering && currentImage != null) {
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw rounded background
            g2.setColor(COLOR_PREVIEW_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

            // Clip to rounded rectangle to prevent image overflow
            g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

            if (currentImage == null) {
                drawPlaceholder(g2);
                g2.dispose();
                return;
            }

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int imgWidth = currentImage.getWidth();
            int imgHeight = currentImage.getHeight();
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            // Base scale to fit image in panel with some padding
            int availWidth = panelWidth - 32;  // 16px padding on each side
            int availHeight = panelHeight - 32;

            if (availWidth <= 0 || availHeight <= 0) {
                g2.dispose();
                return;
            }

            double baseScale = Math.min(
                    (double) availWidth / imgWidth,
                    (double) availHeight / imgHeight
            );

            // Calculate base-scaled image dimensions and position (centered)
            int baseScaledWidth = (int) (imgWidth * baseScale);
            int baseScaledHeight = (int) (imgHeight * baseScale);
            int baseX = (panelWidth - baseScaledWidth) / 2;
            int baseY = (panelHeight - baseScaledHeight) / 2;

            // Check if mouse is within the image bounds (when hovering)
            boolean mouseOverImage = isHovering &&
                    mouseX >= baseX && mouseX < baseX + baseScaledWidth &&
                    mouseY >= baseY && mouseY < baseY + baseScaledHeight;

            if (!mouseOverImage) {
                // ═══════════════════════════════════════════════════════════════
                // Normal fit-to-card view: draw entire image scaled to fit
                // ═══════════════════════════════════════════════════════════════
                g2.drawImage(currentImage, baseX, baseY, baseScaledWidth, baseScaledHeight, null);
            } else {
                // ═══════════════════════════════════════════════════════════════
                // Hover zoom view: 2x zoom centered on mouse position
                // ═══════════════════════════════════════════════════════════════

                // Convert mouse position to original image coordinates
                double imgMouseX = (mouseX - baseX) / baseScale;
                double imgMouseY = (mouseY - baseY) / baseScale;

                // Clamp to image bounds
                imgMouseX = Math.max(0, Math.min(imgWidth - 1, imgMouseX));
                imgMouseY = Math.max(0, Math.min(imgHeight - 1, imgMouseY));

                // Calculate zoomed scale and dimensions
                double zoomedScale = baseScale * HOVER_ZOOM_FACTOR;
                int zoomedWidth = (int) (imgWidth * zoomedScale);
                int zoomedHeight = (int) (imgHeight * zoomedScale);

                // Position the zoomed image so that the point under the mouse
                // appears at the center of the panel
                int zoomedImgX = (int) (panelWidth / 2.0 - imgMouseX * zoomedScale);
                int zoomedImgY = (int) (panelHeight / 2.0 - imgMouseY * zoomedScale);

                // Draw the zoomed image
                g2.drawImage(currentImage, zoomedImgX, zoomedImgY, zoomedWidth, zoomedHeight, null);

                // ═══════════════════════════════════════════════════════════════
                // Draw a subtle crosshair at the center to show zoom focus point
                // ═══════════════════════════════════════════════════════════════
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(1));
                int centerX = panelWidth / 2;
                int centerY = panelHeight / 2;
                int crossSize = 20;
                g2.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY);
                g2.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize);
            }

            g2.dispose();
        }

        private void drawPlaceholder(Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            // Draw image icon placeholder
            g2.setColor(COLOR_TEXT_MUTED);
            g2.setStroke(new BasicStroke(2));
            int iconSize = 64;
            int iconX = centerX - iconSize / 2;
            int iconY = centerY - iconSize / 2 - 40;
            g2.drawRoundRect(iconX, iconY, iconSize, iconSize, 8, 8);

            // Draw mountain shape inside
            int[] xPoints = {iconX + 12, iconX + 32, iconX + 52};
            int[] yPoints = {iconY + 48, iconY + 28, iconY + 48};
            g2.drawPolyline(xPoints, yPoints, 3);

            // Draw sun
            g2.fillOval(iconX + 40, iconY + 14, 12, 12);

            // Draw main text
            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            String mainText = "Drop an image or click \"Select Image\"";
            FontMetrics fm = g2.getFontMetrics();
            int textX = centerX - fm.stringWidth(mainText) / 2;
            int textY = centerY + 20;
            g2.drawString(mainText, textX, textY);

            // Draw subtitle
            g2.setColor(COLOR_TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            String subText = "Hover over loaded image to zoom in";
            fm = g2.getFontMetrics();
            textX = centerX - fm.stringWidth(subText) / 2;
            textY = centerY + 44;
            g2.drawString(subText, textX, textY);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helper: Create Rounded Card Panel
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createRoundedCardPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS));
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // File Chooser Row
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createFileChooserRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fileNameLabel = new JLabel("No file chosen");
        fileNameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fileNameLabel.setForeground(COLOR_TEXT_MUTED);

        JButton selectImageBtn = createInputButton("Select Image…");
        selectImageBtn.addActionListener(e -> {
            BufferedImage img = InputImage.loadImage();
            if (img != null) {
                currentImage = img;
                zoom = 1.0;
                updatePreview();
                saveImageBtn.setEnabled(false);
                previewInfoLabel.setText(img.getWidth() + " × " + img.getHeight() + " px • Original");
                fileNameLabel.setText("Image loaded");
            }
        });

        row.add(selectImageBtn, BorderLayout.WEST);
        row.add(fileNameLabel, BorderLayout.CENTER);

        return row;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI Helper Methods
    // ═══════════════════════════════════════════════════════════════════════════

    private JLabel createTitleLabel(String txt) {
        JLabel label = new JLabel(txt);
        label.setForeground(COLOR_TEXT_TITLE);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createSectionLabel(String txt) {
        JLabel label = new JLabel(txt);
        label.setForeground(COLOR_TEXT_PRIMARY);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton createInputButton(String txt) {
        JButton btn = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(COLOR_TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 32));
        return btn;
    }

    private static JButton createStyledButton(String txt, boolean primary) {
        JButton btn = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = primary ? COLOR_BUTTON_PRIMARY_BG : COLOR_INPUT;
                Color fg = primary ? COLOR_BUTTON_PRIMARY_TEXT : COLOR_TEXT_PRIMARY;

                if (!isEnabled()) {
                    bg = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 100);
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(isEnabled() ? fg : new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 100));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(0, 40));
        return btn;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(COLOR_INPUT);
        combo.setForeground(COLOR_TEXT_PRIMARY);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setFocusable(false);
        combo.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
    }

    private JPanel wrapComboBox(JComboBox<String> combo) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        wrapper.setPreferredSize(new Dimension(0, 36));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        wrapper.add(combo, BorderLayout.CENTER);
        return wrapper;
    }

    private JSlider createStyledSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int trackY = getHeight() / 2 - 3;
                int trackHeight = 6;

                // Track background
                g2.setColor(COLOR_TRACK);
                g2.fillRoundRect(8, trackY, getWidth() - 16, trackHeight, trackHeight, trackHeight);

                // Thumb position
                double percent = (double) (getValue() - getMinimum()) / (getMaximum() - getMinimum());
                int thumbX = (int) (8 + percent * (getWidth() - 32));

                // Thumb
                g2.setColor(Color.WHITE);
                g2.fillOval(thumbX, trackY - 5, 16, 16);
                g2.setColor(new Color(180, 180, 180));
                g2.drawOval(thumbX, trackY - 5, 16, 16);

                g2.dispose();
            }
        };
        slider.setOpaque(false);
        slider.setFocusable(false);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        return slider;
    }

    private JPanel createScaleLabelsPanel() {
        JPanel labelsPanel = new JPanel(new GridLayout(1, 4));
        labelsPanel.setOpaque(false);
        labelsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        labelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] labels = {"2x", "4x", "6x", "8x"};
        for (String label : labels) {
            JLabel l = new JLabel(label, SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.PLAIN, 11));
            l.setForeground(COLOR_TEXT_MUTED);
            labelsPanel.add(l);
        }
        return labelsPanel;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SIMPLIFIED: updatePreview() now just triggers a repaint
    // ═══════════════════════════════════════════════════════════════════════════
    private void updatePreview() {
        if (hoverZoomPanel != null) {
            hoverZoomPanel.repaint();
        }
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