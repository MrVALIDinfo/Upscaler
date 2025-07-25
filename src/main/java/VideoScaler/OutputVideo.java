package VideoScaler;

import javax.swing.*;
import java.io.File;

public class OutputVideo {

    public static File chooseSaveLocation(String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Upscaled Video As");
        chooser.setSelectedFile(new File(defaultName.endsWith(".mp4") ? defaultName : defaultName + ".mp4"));

        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".mp4")) {
                file = new File(file.getAbsolutePath() + ".mp4");
            }
            return file;
        }
        return null;
    }
}