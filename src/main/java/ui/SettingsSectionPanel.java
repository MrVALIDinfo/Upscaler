package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SettingsSectionPanel extends JPanel {

    private final JComboBox<String> themeSelect;
    private final JButton applyThemeButton;

    public SettingsSectionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(40, 44, 52));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("⚙️ Настройки");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(title);
        add(Box.createVerticalStrut(30));

        themeSelect = new JComboBox<>(new String[]{
                "Flat Dark", "Flat Light", "Arc Dark", "Arc Light"
        });
        themeSelect.setMaximumSize(new Dimension(300, 30));
        themeSelect.setBackground(new Color(60, 63, 65));
        themeSelect.setForeground(Color.WHITE);
        themeSelect.setFont(new Font("SansSerif", Font.PLAIN, 14));
        themeSelect.setFocusable(false);
        themeSelect.setAlignmentX(Component.CENTER_ALIGNMENT);

        applyThemeButton = new JButton("Применить тему");
        applyThemeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        applyThemeButton.setFocusPainted(false);
        applyThemeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyThemeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        applyThemeButton.addActionListener(e -> {
            try {
                String selected = (String) themeSelect.getSelectedItem();
                switch (selected) {
                    case "Flat Dark" -> UIManager.setLookAndFeel(new FlatDarkLaf());
                    case "Flat Light" -> UIManager.setLookAndFeel(new FlatLightLaf());
                    case "Arc Dark" -> UIManager.setLookAndFeel(new FlatArcDarkIJTheme());
                    case "Arc Light" -> UIManager.setLookAndFeel(new FlatArcIJTheme());
                }

                SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(this));
                JOptionPane.showMessageDialog(this, "Тема применена!", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при смене темы.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });


    }
}