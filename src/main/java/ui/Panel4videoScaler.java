package ui;

import javax.swing.*;
import java.awt.*;

public class Panel4videoScaler extends JDialog {

    private final JComboBox<String> modelBox;
    private final JButton applyButton;
    private final JButton cancelButton;

    private String selectedModel = null;

    public Panel4videoScaler(Frame parent) {
        super(parent, "Video Upscale Settings", true);
        setUndecorated(true);
        setSize(360, 200);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 44, 52));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Select AI Model:");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        modelBox = new JComboBox<>(new String[]{
                "realesr-animevideov3-x2",
                "realesr-animevideov3-x3",
                "realesr-animevideov3-x4"
        });
        modelBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        modelBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        modelBox.setBackground(new Color(60, 63, 65));
        modelBox.setForeground(Color.WHITE);
        modelBox.setFocusable(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttons.setBackground(panel.getBackground());

        applyButton = new JButton("Apply");
        cancelButton = new JButton("Cancel");

        applyButton.addActionListener(e -> {
            selectedModel = (String) modelBox.getSelectedItem();
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            selectedModel = null;
            setVisible(false);
        });

        buttons.add(applyButton);
        buttons.add(cancelButton);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(modelBox);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttons);

        setContentPane(panel);
    }

    public String getSelectedModel() {
        return selectedModel;
    }
}