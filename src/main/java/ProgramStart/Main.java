package ProgramStart;

import ui.GeneralWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Запускаем GUI
        SwingUtilities.invokeLater(GeneralWindow::new);
    }
}