package ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class NavigationPanel extends JPanel {

    private java.util.function.Consumer<String> navigationListener;

    public NavigationPanel() {
        setBackground(new Color(35, 39, 47));
        setPreferredSize(new Dimension(200, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        File logoFile = new File("src/main/java/res/logo.png");
        JLabel icon;
        if (logoFile.exists()) {
            ImageIcon rawIcon = new ImageIcon(logoFile.getAbsolutePath());
            Image scaledImage = rawIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            icon = new JLabel(new ImageIcon(scaledImage));
        } else {
            icon = new JLabel();
        }
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title = new JLabel("Upscaler", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Dialog", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(30));
        add(icon);
        add(Box.createVerticalStrut(10));
        add(title);
        add(Box.createVerticalStrut(30));

        add(createNavButton("Images", "images"));
        add(createNavButton("Videos", "videos"));
        add(createNavButton("Settings", "settings"));
        add(Box.createVerticalGlue());
    }

    private JButton createNavButton(String label, String sectionName) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(160, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(53, 55, 61));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Dialog", Font.PLAIN, 16));

        btn.addActionListener(e -> {
            if (navigationListener != null) {
                navigationListener.accept(sectionName);
            }
        });

        return btn;
    }

    public void setNavigationListener(java.util.function.Consumer<String> listener) {
        this.navigationListener = listener;
    }
}