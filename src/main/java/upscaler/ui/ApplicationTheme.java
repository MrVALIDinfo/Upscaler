package upscaler.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public final class ApplicationTheme {
    private ApplicationTheme() {
    }

    public static void install() {
        FlatDarkLaf.setup();

        Font baseFont = loadFont("/assets/fonts/Manrope.ttf", 14f);
        UIManager.put("defaultFont", new FontUIResource(baseFont));
        UIManager.put("Component.arc", 20);
        UIManager.put("Button.arc", 20);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("CheckBox.arc", 12);
        UIManager.put("ProgressBar.arc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("TextComponent.arc", 18);
        UIManager.put("ComboBox.arc", 18);
        UIManager.put("PasswordField.arc", 18);
        UIManager.put("TextField.arc", 18);
        UIManager.put("TabbedPane.tabArc", 18);
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.selectedForeground", UiPalette.TEXT);
        UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(255, 255, 255, 22));
        UIManager.put("TabbedPane.underlineColor", UiPalette.ACCENT);
        UIManager.put("TabbedPane.hoverColor", new java.awt.Color(255, 255, 255, 12));
        UIManager.put("TabbedPane.focusColor", UiPalette.ACCENT);
        UIManager.put("Panel.background", UiPalette.WINDOW);
        UIManager.put("RootPane.background", UiPalette.WINDOW);
        UIManager.put("Button.background", UiPalette.SURFACE_SOFT);
        UIManager.put("Button.foreground", UiPalette.TEXT);
        UIManager.put("Button.focusedBorderColor", UiPalette.ACCENT);
        UIManager.put("Button.hoverBackground", new java.awt.Color(255, 255, 255, 20));
        UIManager.put("TextField.background", UiPalette.SURFACE_SOFT);
        UIManager.put("TextField.foreground", UiPalette.TEXT);
        UIManager.put("ComboBox.background", UiPalette.SURFACE_SOFT);
        UIManager.put("ComboBox.foreground", UiPalette.TEXT);
        UIManager.put("Spinner.background", UiPalette.SURFACE_SOFT);
        UIManager.put("Spinner.foreground", UiPalette.TEXT);
        UIManager.put("ProgressBar.background", new java.awt.Color(255, 255, 255, 16));
        UIManager.put("ProgressBar.foreground", UiPalette.ACCENT);
        UIManager.put("ScrollPane.background", UiPalette.SURFACE_SOFT);
        UIManager.put("TextArea.background", UiPalette.SURFACE_SOFT);
        UIManager.put("TextArea.foreground", UiPalette.TEXT);
        UIManager.put("MenuBar.background", new java.awt.Color(10, 14, 18));
        UIManager.put("MenuItem.selectionBackground", new java.awt.Color(255, 255, 255, 18));
        UIManager.put("PopupMenu.background", new java.awt.Color(20, 25, 32));
        UIManager.put("CheckBox.foreground", UiPalette.TEXT);
        UIManager.put("Label.foreground", UiPalette.TEXT);
        UIManager.put("Component.focusColor", UiPalette.ACCENT);
        UIManager.put("Component.borderColor", UiPalette.BORDER);
        UIManager.put("MenuBar.border", BorderFactory.createEmptyBorder(0, 0, 0, 0));
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put(FlatClientProperties.STYLE, "showRevealButton:true");
    }

    private static Font loadFont(String resourcePath, float size) {
        try (InputStream input = ApplicationTheme.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return new Font("SansSerif", Font.PLAIN, Math.round(size));
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(size);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (FontFormatException | IOException ignored) {
            return new Font("SansSerif", Font.PLAIN, Math.round(size));
        }
    }
}
