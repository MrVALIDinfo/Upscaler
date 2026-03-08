package upscaler.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ComparePreviewPanel extends JPanel {
    private static final int PADDING = 28;
    private static final int HANDLE_SIZE = 18;

    private BufferedImage beforeImage;
    private BufferedImage afterImage;
    private double dividerFraction = 0.5;
    private boolean dragging;

    public ComparePreviewPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(960, 720));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragging = true;
                updateDivider(e.getX());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateDivider(e.getX());
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public void setImages(BufferedImage beforeImage, BufferedImage afterImage) {
        this.beforeImage = beforeImage;
        this.afterImage = afterImage;
        repaint();
    }

    public void resetDivider() {
        dividerFraction = 0.5;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2.setColor(UiPalette.PREVIEW);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 34, 34));

        if (beforeImage == null && afterImage == null) {
            drawPlaceholder(g2);
            g2.dispose();
            return;
        }

        Rectangle imageBounds = calculateBounds();
        Shape oldClip = g2.getClip();
        g2.setClip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 34, 34));

        BufferedImage baseImage = afterImage != null ? afterImage : beforeImage;
        g2.drawImage(baseImage, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height, null);

        if (beforeImage != null && afterImage != null) {
            int dividerX = (int) Math.round(imageBounds.x + imageBounds.width * dividerFraction);
            g2.setClip(new Rectangle(imageBounds.x, imageBounds.y, dividerX - imageBounds.x, imageBounds.height));
            g2.drawImage(beforeImage, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height, null);
            g2.setClip(oldClip);
            drawDivider(g2, imageBounds, dividerX);
        }

        g2.setClip(oldClip);
        drawLegend(g2, imageBounds);
        g2.dispose();
    }

    private Rectangle calculateBounds() {
        BufferedImage reference = afterImage != null ? afterImage : beforeImage;
        int availableWidth = Math.max(1, getWidth() - PADDING * 2);
        int availableHeight = Math.max(1, getHeight() - PADDING * 2);
        double scale = Math.min((double) availableWidth / reference.getWidth(), (double) availableHeight / reference.getHeight());
        int width = Math.max(1, (int) Math.round(reference.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(reference.getHeight() * scale));
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        return new Rectangle(x, y, width, height);
    }

    private void drawDivider(Graphics2D g2, Rectangle bounds, int dividerX) {
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(dividerX, bounds.y, dividerX, bounds.y + bounds.height);
        g2.fillOval(dividerX - HANDLE_SIZE / 2, bounds.y + bounds.height / 2 - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
        g2.setColor(UiPalette.ACCENT_DARK);
        g2.drawOval(dividerX - HANDLE_SIZE / 2, bounds.y + bounds.height / 2 - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
    }

    private void drawLegend(Graphics2D g2, Rectangle bounds) {
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        drawTag(g2, bounds.x + 14, bounds.y + 14, "Before", new Color(23, 31, 40, 210), Color.WHITE);
        if (afterImage != null) {
            drawTag(g2, bounds.x + bounds.width - 102, bounds.y + 14, "After", new Color(16, 120, 107, 220), Color.WHITE);
        }
    }

    private void drawTag(Graphics2D g2, int x, int y, String text, Color bg, Color fg) {
        FontMetrics metrics = g2.getFontMetrics();
        int width = metrics.stringWidth(text) + 20;
        int height = 26;
        g2.setColor(bg);
        g2.fillRoundRect(x, y, width, height, 18, 18);
        g2.setColor(fg);
        g2.drawString(text, x + 10, y + 17);
    }

    private void drawPlaceholder(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 210));
        g2.setFont(getFont().deriveFont(Font.BOLD, 26f));
        String title = "Drop an image or open one from File";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (getWidth() - titleMetrics.stringWidth(title)) / 2;
        int titleY = getHeight() / 2 - 12;
        g2.drawString(title, titleX, titleY);

        g2.setColor(new Color(220, 228, 237, 180));
        g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
        String subtitle = "The preview becomes a compare slider automatically after upscaling.";
        FontMetrics subtitleMetrics = g2.getFontMetrics();
        int subtitleX = (getWidth() - subtitleMetrics.stringWidth(subtitle)) / 2;
        g2.drawString(subtitle, subtitleX, titleY + 28);
    }

    private void updateDivider(int mouseX) {
        Rectangle bounds = calculateBounds();
        if (bounds.width <= 0) {
            return;
        }
        dividerFraction = Math.max(0d, Math.min(1d, (mouseX - bounds.x) / (double) bounds.width));
        repaint();
    }
}
