package upscaler.ui;

import upscaler.model.VideoMetadata;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;

public class VideoPosterPanel extends JPanel {
    private BufferedImage thumbnail;
    private VideoMetadata metadata;

    public VideoPosterPanel() {
        setOpaque(false);
    }

    public void setContent(BufferedImage thumbnail, VideoMetadata metadata) {
        this.thumbnail = thumbnail;
        this.metadata = metadata;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        Shape clip = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 34, 34);
        g2.setColor(UiPalette.PREVIEW);
        g2.fill(clip);
        g2.setClip(clip);

        if (thumbnail != null) {
            double scale = Math.max(getWidth() / (double) thumbnail.getWidth(), getHeight() / (double) thumbnail.getHeight());
            int width = (int) Math.round(thumbnail.getWidth() * scale);
            int height = (int) Math.round(thumbnail.getHeight() * scale);
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;
            g2.drawImage(thumbnail, x, y, width, height, null);
            g2.setColor(new Color(18, 22, 28, 120));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2.setColor(new Color(28, 36, 45));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.setColor(new Color(255, 255, 255, 228));
        g2.setFont(getFont().deriveFont(Font.BOLD, 28f));
        String title = thumbnail == null ? "Video Lab" : "Ready to render";
        FontMetrics titleMetrics = g2.getFontMetrics();
        g2.drawString(title, 28, getHeight() - 84);

        g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
        String subtitle = thumbnail == null
                ? "Load a clip to upscale quality and interpolate motion."
                : metadata.resolutionLabel() + "  •  " + metadata.fpsLabel() + "  •  " + metadata.durationLabel();
        g2.drawString(subtitle, 28, getHeight() - 56);

        drawBadge(g2, 28, 26, "VIDEO", new Color(197, 108, 64, 220));
        if (metadata != null) {
            drawBadge(g2, 118, 26, metadata.hasAudio() ? "Audio kept" : "No audio", new Color(16, 120, 107, 220));
        }
        g2.dispose();
    }

    private void drawBadge(Graphics2D g2, int x, int y, String text, Color color) {
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics metrics = g2.getFontMetrics();
        int width = metrics.stringWidth(text) + 20;
        g2.setColor(color);
        g2.fillRoundRect(x, y, width, 26, 18, 18);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + 10, y + 17);
    }
}
