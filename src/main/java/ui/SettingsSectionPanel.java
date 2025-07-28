package ui;

import javax.swing.*;
import java.awt.*;

public class SettingsSectionPanel extends JPanel {

    public SettingsSectionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(40, 44, 52));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("⚙️ Settings");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(title);
        add(Box.createVerticalStrut(30));

        JLabel infoLabel = new JLabel("<html><div style='text-align: center;'>This app is so easy to use,<br>it doesn't even need settings ¯\\_(ツ)_/¯</div></html>");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        infoLabel.setForeground(Color.LIGHT_GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(infoLabel);
    }
}