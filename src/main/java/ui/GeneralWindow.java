package ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GeneralWindow extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JButton> tabButtons = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════════════════
    // Color Palette (matching target design)
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Color COLOR_BG_MAIN = new Color(15, 23, 42);           // #0F172A
    private static final Color COLOR_APPBAR = new Color(17, 24, 39);            // #111827
    private static final Color COLOR_CARD = new Color(39, 46, 63);              // #272E3F
    private static final Color COLOR_INPUT = new Color(59, 66, 84);             // #3B4254
    private static final Color COLOR_TEXT_PRIMARY = new Color(229, 231, 235);   // #E5E7EB
    private static final Color COLOR_TEXT_MUTED = new Color(156, 163, 175);     // #9CA3AF
    private static final Color COLOR_TAB_ACTIVE_BG = new Color(229, 231, 235);  // #E5E7EB
    private static final Color COLOR_TAB_ACTIVE_TEXT = new Color(17, 24, 39);   // #111827

    // Legacy color references (kept for compatibility)
    private static final Color BG_DARK = COLOR_BG_MAIN;
    private static final Color BG_NAV = COLOR_APPBAR;
    private static final Color BG_TAB_DEFAULT = new Color(30, 41, 59);          // #1E293B
    private static final Color BG_TAB_ACTIVE = COLOR_TAB_ACTIVE_BG;
    private static final Color TEXT_PRIMARY = COLOR_TEXT_PRIMARY;
    private static final Color TEXT_MUTED = COLOR_TEXT_MUTED;

    public GeneralWindow() {
        FlatDarkLaf.setup();

        setTitle("Upscaler");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);
        getContentPane().setBackground(COLOR_BG_MAIN);

        JPanel topBar = createAppBar();
        add(topBar, BorderLayout.NORTH);

        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(COLOR_BG_MAIN);
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        JPanel tabsRow = createTabsRow();
        mainWrapper.add(tabsRow, BorderLayout.NORTH);

        contentPanel.setBackground(COLOR_BG_MAIN);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        contentPanel.add(new ImageSectionPanel(), "imageupscaler");
        contentPanel.add(new SettingsSectionPanel(), "settings");

        mainWrapper.add(contentPanel, BorderLayout.CENTER);
        add(mainWrapper, BorderLayout.CENTER);

        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // App Bar
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createAppBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_APPBAR);
        topBar.setPreferredSize(new Dimension(0, 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // ─── Left: logo + title ───
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.X_AXIS));

        JPanel logoPanel = createLogoPanel();
        brandPanel.add(logoPanel);
        brandPanel.add(Box.createHorizontalStrut(12));

        JLabel title = new JLabel("Upscaler");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(COLOR_TEXT_PRIMARY);
        brandPanel.add(title);

        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(brandPanel);
        topBar.add(leftWrapper, BorderLayout.WEST);

        // ─── Center: search bar placeholder ───
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        JPanel searchBar = createSearchBarPlaceholder();
        centerPanel.add(searchBar);
        topBar.add(centerPanel, BorderLayout.CENTER);

        // ─── Right: hint + avatar ───
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        JPanel rightContent = new JPanel();
        rightContent.setOpaque(false);
        rightContent.setLayout(new BoxLayout(rightContent, BoxLayout.X_AXIS));

        JLabel rightHint = new JLabel("v1.0  •  Real-ESRGAN");
        rightHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rightHint.setForeground(TEXT_MUTED);
        rightContent.add(rightHint);
        rightContent.add(Box.createHorizontalStrut(16));

        JPanel avatar = createAvatarPlaceholder();
        rightContent.add(avatar);

        rightPanel.add(rightContent);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    /**
     * Логотип слева от текста "Upscaler".
     * logo.png масштабируется ПРОПОРЦИОНАЛЬНО под max 48x48,
     * без вытягивания, и центрируется в квадратной панели.
     */
    private JPanel createLogoPanel() {
        final int MAX_SIZE = 135;

        ImageIcon icon = null;
        try {
            java.net.URL url = GeneralWindow.class.getResource("/ui/logo.png");
            if (url != null) {
                icon = new ImageIcon(url);
            }
        } catch (Exception ignored) {
        }

        if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
            int imgW = icon.getIconWidth();
            int imgH = icon.getIconHeight();

            double scale = Math.min(
                    (double) MAX_SIZE / imgW,
                    (double) MAX_SIZE / imgH
            );

            int newW = (int) Math.round(imgW * scale);
            int newH = (int) Math.round(imgH * scale);

            Image scaled = icon.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaled);

            JLabel logoLabel = new JLabel(scaledIcon);
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setVerticalAlignment(SwingConstants.CENTER);

            JPanel logoPanel = new JPanel(new BorderLayout()) {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(MAX_SIZE, MAX_SIZE);
                }

                @Override
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }

                @Override
                public Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
            logoPanel.setOpaque(false);
            logoPanel.add(logoLabel, BorderLayout.CENTER);
            return logoPanel;
        }

        // Fallback: старый градиентный квадрат с буквой U
        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(99, 102, 241),
                        40, 40, new Color(168, 85, 247)));
                g2.fillRoundRect(0, 0, 40, 40, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String text = "U";
                int x = (40 - fm.stringWidth(text)) / 2;
                int y = (40 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, x, y);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(70, 70);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        logo.setOpaque(false);
        return logo;
    }

    private JPanel createSearchBarPlaceholder() {
        JPanel searchBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(320, 40);
            }
        };
        searchBar.setOpaque(false);
        return searchBar;
    }

    private JPanel createAvatarPlaceholder() {
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_INPUT);
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(COLOR_TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String text = "U";
                int x = (32 - fm.stringWidth(text)) / 2;
                int y = (32 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, x, y);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        avatar.setOpaque(false);
        return avatar;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Tabs Row
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createTabsRow() {
        JPanel tabsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabsRow.setOpaque(false);
        tabsRow.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        JPanel tabContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_TAB_DEFAULT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        tabContainer.setOpaque(false);
        tabContainer.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        addTabButton(tabContainer, "Image Upscaler", "imageupscaler", true);
        addTabButton(tabContainer, "Settings", "settings", false);

        tabsRow.add(tabContainer);
        return tabsRow;
    }

    private void addTabButton(JPanel parent, String title, String key, boolean selected) {
        JButton tabButton = new JButton(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

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
        tabButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabButton.setFocusPainted(false);
        tabButton.setBorderPainted(false);
        tabButton.setContentAreaFilled(false);
        tabButton.setOpaque(false);
        tabButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tabButton.setPreferredSize(new Dimension(140, 36));

        updateTabButtonStyle(tabButton, selected);

        tabButton.addActionListener(e -> {
            cardLayout.show(contentPanel, key);
            setActiveTab(key);
        });

        parent.add(tabButton);
        tabButtons.put(key, tabButton);
    }

    private void updateTabButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(BG_TAB_ACTIVE);
            btn.setForeground(COLOR_TAB_ACTIVE_TEXT);
        } else {
            btn.setBackground(new Color(0, 0, 0, 0));
            btn.setForeground(TEXT_PRIMARY);
        }
    }

    private void setActiveTab(String activeKey) {
        for (var entry : tabButtons.entrySet()) {
            String key = entry.getKey();
            JButton btn = entry.getValue();
            updateTabButtonStyle(btn, key.equals(activeKey));
            btn.repaint();
        }
    }
}
