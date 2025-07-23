package ui;

import javax.swing.*;
import java.awt.*;

public class GeneralWindow extends JFrame {

    public GeneralWindow() {
        setTitle("My Swing Window");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        setVisible(true);
    }
}
