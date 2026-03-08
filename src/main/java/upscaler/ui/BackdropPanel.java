package upscaler.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

public class BackdropPanel extends JPanel {
    public BackdropPanel(LayoutManager layout) {
        super(layout);
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(UiPalette.WINDOW);
        g2.fillRect(0, 0, getWidth(), getHeight());

        paintGlow(g2, getWidth() * 0.18f, getHeight() * 0.12f, Math.max(220f, getWidth() * 0.28f),
                new Color(UiPalette.ACCENT.getRed(), UiPalette.ACCENT.getGreen(), UiPalette.ACCENT.getBlue(), 110));
        paintGlow(g2, getWidth() * 0.86f, getHeight() * 0.18f, Math.max(180f, getWidth() * 0.20f),
                new Color(UiPalette.WARM.getRed(), UiPalette.WARM.getGreen(), UiPalette.WARM.getBlue(), 70));
        paintGlow(g2, getWidth() * 0.50f, getHeight() * 0.92f, Math.max(260f, getWidth() * 0.32f),
                new Color(76, 120, 255, 55));

        g2.dispose();
    }

    private void paintGlow(Graphics2D g2, float centerX, float centerY, float radius, Color color) {
        RadialGradientPaint glow = new RadialGradientPaint(
                new Point2D.Float(centerX, centerY),
                radius,
                new float[]{0f, 1f},
                new Color[]{color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)}
        );
        g2.setPaint(glow);
        int diameter = Math.round(radius * 2);
        g2.fillOval(Math.round(centerX - radius), Math.round(centerY - radius), diameter, diameter);
    }
}
