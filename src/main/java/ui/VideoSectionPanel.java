package ui;

import javax.swing.*;
import java.awt.*;

public class VideoSectionPanel extends JPanel {

    private final JLabel videoLabel = new JLabel("No video loaded", SwingConstants.CENTER);
    private final JProgressBar progressBar;
    private double zoom = 1.0;

    private static final int PREVIEW_W = 800;
    private static final int PREVIEW_H = 600;

    public VideoSectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(40, 44, 52));

        JLabel title = new JLabel("Upscale Video", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        videoLabel.setVerticalAlignment(SwingConstants.CENTER);
        videoLabel.setForeground(Color.GRAY);

        JScrollPane previewScroll = new JScrollPane(videoLabel);
        previewScroll.setPreferredSize(new Dimension(PREVIEW_W, PREVIEW_H));
        previewScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65)));
        previewScroll.getViewport().setBackground(new Color(30, 33, 36));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setForeground(new Color(100, 200, 100));
        progressBar.setPreferredSize(new Dimension(500, 22));
        progressBar.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        progressPanel.setBackground(getBackground());
        progressPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
        progressPanel.add(progressBar);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 14));
        buttons.setBackground(getBackground());

        JButton upload = createMainButton("Open");
        JButton upscale = createMainButton("Upscale");
        JButton clear = createMainButton("Clear");
        JButton save = createMainButton("Save");
        JButton zoomIn = createMainButton("Zoom +");
        JButton zoomOut = createMainButton("Zoom -");

        buttons.add(upload);
        buttons.add(upscale);
        buttons.add(clear);
        buttons.add(save);
        buttons.add(zoomIn);
        buttons.add(zoomOut);

        upload.addActionListener(_ -> {
            videoLabel.setText("Video loaded (demo)");
            videoLabel.setIcon(null);
            zoom = 1.0;
            videoLabel.revalidate();
            videoLabel.repaint();
        });

        upscale.addActionListener(_ -> {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Upscaling...");
            setButtonsEnabled(buttons, false);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    return null;
                }
                @Override
                protected void done() {
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                    setButtonsEnabled(buttons, true);
                    JOptionPane.showMessageDialog(VideoSectionPanel.this, "✅ Demo upscaling complete!");
                }
            };
            worker.execute();
        });

        clear.addActionListener(_ -> {
            videoLabel.setIcon(null);
            videoLabel.setText("No video loaded");
            zoom = 1.0;
            videoLabel.revalidate();
            videoLabel.repaint();
        });

        save.addActionListener(_ -> {
            JOptionPane.showMessageDialog(this, "Demo: Save video (not implemented)");
        });

        zoomIn.addActionListener(_ -> {
            zoom = Math.min(zoom * 1.25, 8.0);
            videoLabel.setText("Zoom: " + String.format("%.2f", zoom) + "x (demo)");
        });

        zoomOut.addActionListener(_ -> {
            zoom = Math.max(zoom / 1.25, 1.0);
            videoLabel.setText("Zoom: " + String.format("%.2f", zoom) + "x (demo)");
        });

        previewScroll.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                zoom = (e.getWheelRotation() < 0)
                        ? Math.min(zoom * 1.1, 8.0)
                        : Math.max(zoom / 1.1, 1.0);
                videoLabel.setText("Zoom: " + String.format("%.2f", zoom) + "x (demo)");
            }
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        center.add(title);
        center.add(Box.createVerticalStrut(10));
        center.add(previewScroll);

        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);
        page.add(center, BorderLayout.CENTER);
        page.add(buttons, BorderLayout.SOUTH);

        add(page, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);
    }

    private void setButtonsEnabled(JPanel panel, boolean enabled) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton b) b.setEnabled(enabled);
        }
    }

    private JButton createMainButton(String text) {
        JButton btn = new JButton(text.toUpperCase());
        btn.setBackground(new Color(60, 63, 65));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}