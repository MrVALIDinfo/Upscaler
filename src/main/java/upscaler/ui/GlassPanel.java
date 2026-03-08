package upscaler.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class GlassPanel extends JPanel {
    private final int arc;

    public GlassPanel() {
        this(null, 32);
    }

    public GlassPanel(LayoutManager layout) {
        this(layout, 32);
    }

    public GlassPanel(LayoutManager layout, int arc) {
        super(layout);
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(
                0,
                0,
                new Color(UiPalette.SURFACE.getRed(), UiPalette.SURFACE.getGreen(), UiPalette.SURFACE.getBlue(), 230),
                getWidth(),
                getHeight(),
                new Color(UiPalette.SURFACE_SOFT.getRed(), UiPalette.SURFACE_SOFT.getGreen(), UiPalette.SURFACE_SOFT.getBlue(), 210)
        );
        g2.setPaint(background);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        g2.setColor(new Color(255, 255, 255, 22));
        g2.fillRoundRect(1, 1, getWidth() - 2, Math.max(18, getHeight() / 4), arc, arc);

        g2.setColor(UiPalette.BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();

        super.paintComponent(g);
    }
}
