package ui;

import javax.swing.*;
import java.awt.*;

public class VideoSectionPanel extends JPanel {
    public VideoSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);
        add(new JLabel("🎥 Видео поддержка в разработке", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}