package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Panel4imageScaler extends JDialog {

    private final JRadioButton scale2x;
    private final JRadioButton scale4x;
    private final JRadioButton scale8x;
    private final JComboBox<String> modelBox;
    private final JButton submitButton;
    private final JButton cancelButton;

    private String selectedScale = "4";
    private String selectedModel = "realesrgan-x4plus";

    // ═══════════════════════════════════════════════════════════════════════════
    // Color Palette (matching target design)
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Color COLOR_BG_OVERLAY = new Color(0, 0, 0, 150);
    private static final Color COLOR_CARD = new Color(39, 46, 63);              // #272E3F
    private static final Color COLOR_INPUT = new Color(59, 66, 84);             // #3B4254
    private static final Color COLOR_TEXT_PRIMARY = new Color(229, 231, 235);   // #E5E7EB
    private static final Color COLOR_TEXT_MUTED = new Color(156, 163, 175);     // #9CA3AF
    private static final Color COLOR_TEXT_TITLE = new Color(249, 250, 251);     // #F9FAFB
    private static final Color COLOR_BUTTON_PRIMARY_BG = new Color(229, 231, 235);
    private static final Color COLOR_BUTTON_PRIMARY_TEXT = new Color(17, 24, 39);

    // Legacy references
    private static final Color BG = new Color(24, 26, 34);
    private static final Color CARD = COLOR_CARD;
    private static final Color TEXT = COLOR_TEXT_PRIMARY;
    private static final Color TEXT_MUTED = COLOR_TEXT_MUTED;
    private static final Color ACCENT = new Color(80, 140, 255);

    private static final int CARD_RADIUS = 20;

    public Panel4imageScaler(Frame parent) {
        super(parent, "Upscale Settings", true);

        setUndecorated(true);
        setSize(440, 340);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(COLOR_BG_OVERLAY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS));
                g2.dispose();
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        mainPanel.setPreferredSize(new Dimension(400, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        root.add(mainPanel, gbc);

        JLabel title = new JLabel("Upscale Settings");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(COLOR_TEXT_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Choose model and output scale.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createVerticalStrut(20));

        // Model
        JLabel modelLabel = new JLabel("AI Model");
        modelLabel.setForeground(TEXT);
        modelLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        modelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        modelBox = new JComboBox<>(new String[]{
                "realesrgan-x4plus",
                "realesrgan-x4plus-anime"
        });
        modelBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        modelBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        modelBox.setFocusable(false);
        modelBox.setBackground(COLOR_INPUT);
        modelBox.setForeground(TEXT);
        modelBox.setSelectedIndex(0);
        modelBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(modelLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(wrapComboBox(modelBox));
        mainPanel.add(Box.createVerticalStrut(16));

        // Scale
        JLabel scaleLabel = new JLabel("Scale factor");
        scaleLabel.setForeground(TEXT);
        scaleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        scaleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        scale2x = createStyledRadio("2× (softer, faster)");
        scale4x = createStyledRadio("4× (recommended)");
        scale8x = createStyledRadio("8× (heavy, slow)");
        scale4x.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(scale2x);
        group.add(scale4x);
        group.add(scale8x);

        mainPanel.add(scaleLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(scale2x);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(scale4x);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(scale8x);

        // Buttons
        mainPanel.add(Box.createVerticalStrut(24));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        cancelButton = createDialogButton("Cancel", false);
        submitButton = createDialogButton("Apply", true);

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);

        mainPanel.add(buttonPanel);

        // Actions
        submitButton.addActionListener(e -> {
            if (scale2x.isSelected()) selectedScale = "2";
            else if (scale4x.isSelected()) selectedScale = "4";
            else if (scale8x.isSelected()) selectedScale = "8";
            selectedModel = (String) modelBox.getSelectedItem();
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            selectedScale = null;
            selectedModel = null;
            setVisible(false);
        });
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
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.setPreferredSize(new Dimension(0, 40));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        wrapper.add(combo, BorderLayout.CENTER);
        return wrapper;
    }

    private JRadioButton createStyledRadio(String label) {
        JRadioButton btn = new JRadioButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(TEXT);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private JButton createDialogButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = primary ? COLOR_BUTTON_PRIMARY_BG : COLOR_INPUT;
                Color fg = primary ? COLOR_BUTTON_PRIMARY_TEXT : TEXT;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 40));
        return btn;
    }

    public String getSelectedScale() {
        return selectedScale;
    }

    public String getSelectedModel() {
        return selectedModel;
    }
}