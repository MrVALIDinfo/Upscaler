package ui;

import javax.swing.*;
import java.awt.*;

public class Panel4imageScaler extends JDialog {

    private final JRadioButton scale2x;
    private final JRadioButton scale4x;
    private final JRadioButton scale8x;
    private final JComboBox<String> modelBox;
    private final JButton submitButton;
    private final JButton cancelButton;

    private String selectedScale = "4";
    private String selectedModel = "realesrgan-x4plus";

    public Panel4imageScaler(Frame parent) {
        super(parent, "Upscale Settings", true);
        setUndecorated(true);
        setSize(380, 280);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(40, 44, 52));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Upscale Settings");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));

        modelBox = new JComboBox<>(new String[]{
                "realesrgan-x4plus",
                "realesrgan-x4plus-anime"
        });
        modelBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        modelBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        modelBox.setFocusable(false);
        modelBox.setBackground(new Color(60, 63, 65));
        modelBox.setForeground(Color.WHITE);
        modelBox.setSelectedIndex(0);

        JLabel modelLabel = new JLabel("Model:");
        modelLabel.setForeground(Color.WHITE);
        modelLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        modelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(modelLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(modelBox);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel scaleLabel = new JLabel("Scale:");
        scaleLabel.setForeground(Color.WHITE);
        scaleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        scaleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        scale2x = createRadio("2x");
        scale4x = createRadio("4x");
        scale8x = createRadio("8x");
        scale4x.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(scale2x);
        group.add(scale4x);
        group.add(scale8x);

        mainPanel.add(scaleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(scale2x);
        mainPanel.add(scale4x);
        mainPanel.add(scale8x);
        mainPanel.add(Box.createVerticalStrut(25));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(mainPanel.getBackground());

        submitButton = createDialogButton("Apply");
        cancelButton = createDialogButton("Cancel");

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel);

        setContentPane(mainPanel);

        submitButton.addActionListener(e -> {
            if (scale2x.isSelected()) selectedScale = "2";
            else if (scale4x.isSelected()) selectedScale = "4";
            else if (scale8x.isSelected()) selectedScale = "8";
            selectedModel = (String) modelBox.getSelectedItem();
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            selectedScale = null;
            selectedModel = null;
            setVisible(false);
        });
    }

    private JRadioButton createRadio(String label) {
        JRadioButton btn = new JRadioButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 44, 52));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createDialogButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(60, 63, 65));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public String getSelectedScale() {
        return selectedScale;
    }

    public String getSelectedModel() {
        return selectedModel;
    }
}