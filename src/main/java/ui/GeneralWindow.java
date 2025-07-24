package ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class GeneralWindow extends JFrame {

    public GeneralWindow() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf()); // 🌑 Темная тема
        } catch (Exception e) {
            System.err.println("Не удалось применить тему FlatLaf :(");
        }

        setTitle("Upscaler");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);
        setResizable(false); // 🔒 запрещаем изменение размера

        // ✅ Глобальный layout
        setLayout(new BorderLayout());

        // === Sidebar (левая панель навигации)
        NavigationPanel sidebar = new NavigationPanel();
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === Content area (правый блок)
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(32, 34, 37));

        // === Секции (по примеру Upscayl)
        ImageSectionPanel imagePanel = new ImageSectionPanel();
        VideoSectionPanel videoPanel = new VideoSectionPanel();
        SettingsSectionPanel settingsPanel = new SettingsSectionPanel();

        contentPanel.add(imagePanel, "images");
        contentPanel.add(videoPanel, "videos");
        contentPanel.add(settingsPanel, "settings");

        // === Навигация (обработка переключения секций)
        sidebar.setNavigationListener(sectionName -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, sectionName);
        });

        // === Добавляем в main window
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // === Инициализация
        setVisible(true);
    }
}