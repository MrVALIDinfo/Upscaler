package upscaler.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class ImageResizer {
    private ImageResizer() {
    }

    public static BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight) {
        if (source.getWidth() == targetWidth && source.getHeight() == targetHeight) {
            return source;
        }

        int imageType = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage current = source;
        int width = source.getWidth();
        int height = source.getHeight();

        while (width != targetWidth || height != targetHeight) {
            int nextWidth = nextDimension(width, targetWidth);
            int nextHeight = nextDimension(height, targetHeight);
            BufferedImage resized = new BufferedImage(nextWidth, nextHeight, imageType);
            Graphics2D g2 = resized.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(current, 0, 0, nextWidth, nextHeight, null);
            g2.dispose();
            current = resized;
            width = nextWidth;
            height = nextHeight;
        }

        return current;
    }

    private static int nextDimension(int current, int target) {
        if (current == target) {
            return target;
        }
        if (current > target) {
            return Math.max(target, current / 2);
        }
        return Math.min(target, current * 2);
    }
}
