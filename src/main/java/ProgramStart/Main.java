// ProgramStart/Main.java
package ProgramStart;

import ui.GeneralWindow;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Не удалось установить FlatDarkLaf: " + e.getMessage());
        }

        SwingUtilities.invokeLater(GeneralWindow::new);
    }
}