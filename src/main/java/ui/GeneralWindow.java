package ui;

import javax.swing.*;
import java.awt.*;

public class GeneralWindow extends JFrame {

    public GeneralWindow() {
        setTitle("Upscaler");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Sidebar (навигатор)
        NavigationPanel sidebar = new NavigationPanel();

        // Content (правая часть)
        JPanel contentPanel = new JPanel(new CardLayout());

        // Sections
        ImageSectionPanel imagePanel = new ImageSectionPanel();
        VideoSectionPanel videoPanel = new VideoSectionPanel();
        SettingsSectionPanel settingsPanel = new SettingsSectionPanel();

        // Добавляем секции в CardLayout
        contentPanel.add(imagePanel, "images");
        contentPanel.add(videoPanel, "videos");
        contentPanel.add(settingsPanel, "settings");

        // Навигация кнопок управления
        sidebar.setNavigationListener(sectionName -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, sectionName);
        });

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }
}