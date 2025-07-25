package VideoScaler;

import javax.swing.*;
import java.io.File;

public class InputVideo {

    private static final String[] SUPPORTED = { "mp4", "mov", "avi", "mkv" };

    public static File loadVideo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a Video File");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                for (String ext : SUPPORTED) {
                    if (name.endsWith("." + ext)) return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Supported Video Files (*.mp4, *.mov, *.avi, *.mkv)";
            }
        });

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (selected.exists()) {
                return selected;
            }
        }
        return null;
    }
}