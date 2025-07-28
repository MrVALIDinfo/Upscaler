package ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class GeneralWindow extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public GeneralWindow() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ignored) {}

        setTitle("🧠 Upscaler Pro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);

        // ===== Top Navigation Tabs ===== //
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topNav.setBackground(new Color(25, 27, 35));

        String[] tabs = { "Image Upscaler", "Video Upscaler", "Settings" };
        for (String tab : tabs) {
            JButton button = new JButton(tab);
            button.setFocusPainted(false);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(40, 44, 52));
            button.setFont(new Font("SansSerif", Font.BOLD, 14));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            button.putClientProperty("JButton.buttonType", "roundRect");

            button.addActionListener(e -> {
                String key = tab.toLowerCase().replace(" ", "");
                cardLayout.show(contentPanel, key);
            });

            topNav.add(button);
        }

        add(topNav, BorderLayout.NORTH);

        // ===== Content Area ===== //
        contentPanel.setBackground(new Color(32, 34, 37));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new ImageSectionPanel(), "imageupscaler");
        contentPanel.add(new VideoSectionPanel(), "videoupscaler");
        contentPanel.add(new SettingsSectionPanel(), "settings");

        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}