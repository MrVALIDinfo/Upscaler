// ui/GeneralWindow.java
package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.awt.*;

public class GeneralWindow extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public GeneralWindow() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ignored) {}

        FlatIntelliJLaf.setup();
        setTitle("🧠 Upscaler Pro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);

        JPanel topNavPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topNavPanel.setBackground(new Color(25, 27, 35));

        String[] tabs = { "Image Upscaler", "Settings" };
        for (String tab : tabs) {
            JButton tabButton = new JButton(tab);
            tabButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            tabButton.setBackground(new Color(40, 44, 52));
            tabButton.setForeground(Color.WHITE);
            tabButton.setFocusPainted(false);
            tabButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabButton.setBorder(BorderFactory.createEmptyBorder(12, 26, 12, 26));
            tabButton.putClientProperty("JButton.buttonType", "roundRect");

            tabButton.addActionListener(e -> {
                String key = tab.toLowerCase().replace(" ", "");
                cardLayout.show(contentPanel, key);
            });

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(new Color(25, 27, 35));
            wrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            wrapper.add(tabButton);
            topNavPanel.add(wrapper);
        }

        add(topNavPanel, BorderLayout.NORTH);

        contentPanel.setBackground(new Color(32, 34, 37));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new ImageSectionPanel(), "imageupscaler");
        contentPanel.add(new SettingsSectionPanel(), "settings");

        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}