package ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GeneralWindow extends JFrame {

    private ImageSectionPanel imagePanel;

    public GeneralWindow() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ignored) {}

        setTitle("Upscaler");
        setPreferredSize(new Dimension(1200, 850));
        setMinimumSize(new Dimension(1100, 750));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        NavigationPanel sidebar = new NavigationPanel();
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(32, 34, 37));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        imagePanel = new ImageSectionPanel();
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (imagePanel != null) {
                    imagePanel.cleanupOnExit();
                }
            }
        });

        pack();
        setVisible(true);
    }

    public void lockWindow() {
        setEnabled(false);
    }

    public void unlockWindow() {
        setEnabled(true);
    }
}