package ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class GeneralWindow extends JFrame {

    public GeneralWindow() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf()); // тёмная тема
        } catch (Exception e) {
            System.err.println("Не удалось применить тему FlatLaf :(");
        }

        setTitle("Upscaler");
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true); // ✅ теперь окно можно масштабировать

        setLayout(new BorderLayout());

        NavigationPanel sidebar = new NavigationPanel();
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(32, 34, 37));

        ImageSectionPanel imagePanel = new ImageSectionPanel();
        VideoSectionPanel videoPanel = new VideoSectionPanel();
        SettingsSectionPanel settingsPanel = new SettingsSectionPanel();

        contentPanel.add(imagePanel, "images");
        contentPanel.add(videoPanel, "videos");
        contentPanel.add(settingsPanel, "settings");

        sidebar.setNavigationListener(section -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, section);
        });

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}