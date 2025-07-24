package ui;

import javax.swing.*;
import java.awt.*;

public class Panel4imageScaler extends JDialog {

    private final JRadioButton scale2x;
    private final JRadioButton scale4x;
    private final JRadioButton scale8x;
    private final JButton submitButton;
    private final JButton cancelButton;
    private final ButtonGroup scaleGroup;
    private String selectedScale = null;

    public Panel4imageScaler(Frame parent) {
        super(parent, "Select Upscale Quality", true);
        setUndecorated(true);
        setSize(350, 300);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Select Upscale Quality"));
        mainPanel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        scale2x = new JRadioButton("2x Quality");
        scale4x = new JRadioButton("4x Quality");
        scale8x = new JRadioButton("8x Quality");
        scale2x.setSelected(true);

        scale2x.setBackground(new Color(245, 245, 245));
        scale4x.setBackground(new Color(245, 245, 245));
        scale8x.setBackground(new Color(245, 245, 245));
        scale2x.setFont(new Font("SansSerif", Font.PLAIN, 14));
        scale4x.setFont(new Font("SansSerif", Font.PLAIN, 14));
        scale8x.setFont(new Font("SansSerif", Font.PLAIN, 14));

        scaleGroup = new ButtonGroup();
        scaleGroup.add(scale2x);
        scaleGroup.add(scale4x);
        scaleGroup.add(scale8x);

        mainPanel.add(scale2x, gbc);
        gbc.gridy++;
        mainPanel.add(scale4x, gbc);
        gbc.gridy++;
        mainPanel.add(scale8x, gbc);

        submitButton = new JButton("Submit");
        submitButton.setFocusPainted(false);
        submitButton.setBackground(Color.GRAY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        submitButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        submitButton.setContentAreaFilled(true);
        submitButton.setOpaque(true);

        cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);
        cancelButton.setBackground(Color.GRAY);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.setContentAreaFilled(true);
        cancelButton.setOpaque(true);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        gbc.gridy++;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        setContentPane(mainPanel);

        // Кнопки
        submitButton.addActionListener(e -> {
            if (scale2x.isSelected()) selectedScale = "2x";
            else if (scale4x.isSelected()) selectedScale = "4x";
            else if (scale8x.isSelected()) selectedScale = "8x";
            else selectedScale = null;
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            selectedScale = null;
            setVisible(false);
        });

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public String getSelectedScale() {
        return selectedScale;
    }
}