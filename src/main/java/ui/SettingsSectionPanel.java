package ui;

import javax.swing.*;
import java.awt.*;

public class SettingsSectionPanel extends JPanel {
    public SettingsSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);
        add(new JLabel("⚙️ Настройки пока не реализованы", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}