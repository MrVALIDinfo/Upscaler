package ui;

import javax.swing.*;
import java.awt.*;

public class Panel4imageScaler extends JDialog {

    private final JRadioButton scale2x;
    private final JRadioButton scale4x;
    private final JRadioButton scale8x;
    private final JButton submitButton;
    private final JButton cancelButton;
    private final String selectedDefault = "4x"; // по умолчанию
    private String selectedScale = null;

    public Panel4imageScaler(Frame parent) {
        super(parent, "Select Upscale Quality", true);

        // === Настройки окна
        setUndecorated(false);
        setSize(320, 260);
        setLocationRelativeTo(parent);
        setResizable(false);

        // === Основная панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(40, 44, 52));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Select Upscale Factor");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));

        // Radio buttons
        scale2x = createRadio("2x Quality");
        scale4x = createRadio("4x Quality");
        scale8x = createRadio("8x Quality");

        ButtonGroup group = new ButtonGroup();
        group.add(scale2x);
        group.add(scale4x);
        group.add(scale8x);

        // Значение по умолчанию
        scale4x.setSelected(true);

        mainPanel.add(scale2x);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(scale4x);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(scale8x);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(40, 44, 52));

        submitButton = createDialogButton("Submit");
        cancelButton = createDialogButton("Cancel");

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel);

        setContentPane(mainPanel);

        // === Обработчики
        submitButton.addActionListener(e -> {
            if (scale2x.isSelected()) selectedScale = "2";
            else if (scale4x.isSelected()) selectedScale = "4";
            else if (scale8x.isSelected()) selectedScale = "8";
            else selectedScale = null;
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            selectedScale = null;
            setVisible(false);
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JRadioButton createRadio(String label) {
        JRadioButton btn = new JRadioButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 44, 52));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
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
}