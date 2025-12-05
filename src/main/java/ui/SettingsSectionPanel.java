package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SettingsSectionPanel extends JPanel {

    // ═══════════════════════════════════════════════════════════════════════════
    // Color Palette (matching target design)
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Color COLOR_BG_MAIN = new Color(15, 23, 42);           // #0F172A
    private static final Color COLOR_CARD = new Color(39, 46, 63);              // #272E3F
    private static final Color COLOR_INPUT = new Color(59, 66, 84);             // #3B4254
    private static final Color COLOR_TEXT_PRIMARY = new Color(229, 231, 235);   // #E5E7EB
    private static final Color COLOR_TEXT_MUTED = new Color(156, 163, 175);     // #9CA3AF
    private static final Color COLOR_TEXT_TITLE = new Color(249, 250, 251);     // #F9FAFB

    // Legacy references
    private static final Color BG = COLOR_BG_MAIN;
    private static final Color CARD = COLOR_CARD;
    private static final Color BORDER = new Color(50, 54, 70);
    private static final Color TEXT = COLOR_TEXT_PRIMARY;
    private static final Color TEXT_MUTED = COLOR_TEXT_MUTED;
    private static final Color ACCENT = new Color(80, 140, 255);

    private static final int CARD_RADIUS = 20;

    public SettingsSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Main card
        JPanel mainCard = createRoundedCardPanel();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel title = new JLabel("Settings & Info");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(COLOR_TEXT_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Lightweight tool, minimal setup. Everything works out-of-the-box.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainCard.add(title);
        mainCard.add(Box.createVerticalStrut(8));
        mainCard.add(subtitle);
        mainCard.add(Box.createVerticalStrut(24));

        // Карточки
        mainCard.add(createInfoCard("Rendering & Performance",
                "Real-ESRGAN runs as an external process.\n" +
                        "Upscaling time depends on your GPU/CPU and selected scale.\n\n" +
                        "• For quick preview, use x2 or x4.\n" +
                        "• For final export, you can try x8 (heavier)."));

        mainCard.add(Box.createVerticalStrut(16));

        mainCard.add(createInfoCard("Models",
                "Bundled models:\n" +
                        "• realesrgan-x4plus — general purpose upscaling\n" +
                        "• realesrgan-x4plus-anime — stylized / anime content\n\n" +
                        "Place additional NCNN models into the 'models' folder to extend capabilities."));

        mainCard.add(Box.createVerticalStrut(16));

        mainCard.add(createInfoCard("About",
                "Upscaler Pro UI\n" +
                        "Built on top of Real-ESRGAN NCNN Vulkan.\n\n" +
                        "Tip: You can keep this tool on a second monitor and quickly upscale\n" +
                        "screenshots, textures, or reference images for design, 3D, and games."));

        mainCard.add(Box.createVerticalGlue());

        add(mainCard, BorderLayout.CENTER);
    }

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

    private JPanel createInfoCard(String header, String body) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel h = new JLabel(header);
        h.setFont(new Font("SansSerif", Font.BOLD, 15));
        h.setForeground(TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea text = new JTextArea(body);
        text.setFont(new Font("SansSerif", Font.PLAIN, 12));
        text.setForeground(TEXT_MUTED);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setBorder(null);
        text.setOpaque(false);
        text.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(h);
        card.add(Box.createVerticalStrut(8));
        card.add(text);

        return card;
    }
}