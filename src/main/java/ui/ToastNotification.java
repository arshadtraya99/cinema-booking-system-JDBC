package ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

/** Small non-blocking glass toast with a slide-in/slide-out animation. */
public final class ToastNotification {
    private ToastNotification() { }

    public static void show(Component parent, String title, String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> show(parent, title, message));
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(parent);
        if (owner == null || !owner.isShowing()) return;

        JWindow window = new JWindow(owner);
        window.setFocusableWindowState(false);
        try {
            window.setBackground(new Color(0, 0, 0, 0));
        } catch (UnsupportedOperationException ignored) {
            // Some desktop compositors do not support per-pixel translucency.
        }

        JPanel surface = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new java.awt.GradientPaint(0, 0, new Color(28, 43, 70, 246),
                        getWidth(), getHeight(), new Color(10, 18, 34, 246)));
                g.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 22, 22));
                g.setColor(new Color(UiTheme.MINT.getRed(), UiTheme.MINT.getGreen(), UiTheme.MINT.getBlue(), 125));
                g.fillRoundRect(1, 13, 3, getHeight() - 26, 3, 3);
                g.setColor(new Color(225, 239, 255, 60));
                g.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 22, 22));
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        surface.setOpaque(false);
        surface.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel mark = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new java.awt.GradientPaint(0, 0, UiTheme.MINT, getWidth(), getHeight(), UiTheme.PINK));
                g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g.setColor(Color.WHITE);
                g.setStroke(new java.awt.BasicStroke(2.2f, java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND));
                g.drawLine(8, 15, 13, 20);
                g.drawLine(13, 20, 22, 10);
                g.dispose();
            }
        };
        mark.setOpaque(false);
        mark.setPreferredSize(new java.awt.Dimension(30, 30));
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new javax.swing.BoxLayout(copy, javax.swing.BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title);
        heading.setForeground(UiTheme.TEXT);
        heading.setFont(UiTheme.font(Font.BOLD, 13));
        JLabel detail = new JLabel("<html><div style='width:245px'>" + escapeHtml(message) + "</div></html>");
        detail.setForeground(UiTheme.MUTED);
        detail.setFont(UiTheme.font(Font.PLAIN, 11));
        copy.add(heading);
        copy.add(javax.swing.Box.createVerticalStrut(5));
        copy.add(detail);
        JLabel close = new JLabel("×", SwingConstants.CENTER);
        close.setForeground(UiTheme.MUTED);
        close.setFont(UiTheme.font(Font.PLAIN, 20));
        close.setPreferredSize(new java.awt.Dimension(20, 24));

        surface.add(mark, BorderLayout.WEST);
        surface.add(copy, BorderLayout.CENTER);
        surface.add(close, BorderLayout.EAST);
        window.setContentPane(surface);
        window.setSize(390, 86);
        window.validate();

        int targetX = owner.getX() + owner.getWidth() - window.getWidth() - 24;
        int targetY = owner.getY() + owner.getHeight() - window.getHeight() - 24;
        targetX = Math.max(8, targetX);
        targetY = Math.max(8, targetY);
        final int x = targetX;
        final int y = targetY;
        window.setLocation(x, y + 28);
        window.setVisible(true);
        close.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { window.dispose(); }
        });

        final long started = System.currentTimeMillis();
        Timer timer = new Timer(16, (ActionEvent event) -> {
            long elapsed = System.currentTimeMillis() - started;
            int entrance = 220;
            int hold = 1900;
            int exit = 240;
            if (elapsed < entrance) {
                float t = easeOut(elapsed / (float) entrance);
                window.setLocation(x, y + Math.round(28 * (1f - t)));
                setOpacitySafely(window, 0.25f + 0.75f * t);
            } else if (elapsed < entrance + hold) {
                window.setLocation(x, y);
                setOpacitySafely(window, 1f);
            } else if (elapsed < entrance + hold + exit) {
                float t = (elapsed - entrance - hold) / (float) exit;
                window.setLocation(x, y - Math.round(10 * t));
                setOpacitySafely(window, 1f - t);
            } else {
                ((Timer) event.getSource()).stop();
                window.dispose();
            }
        });
        timer.setCoalesce(true);
        timer.start();
    }

    private static void setOpacitySafely(Window window, float opacity) {
        try {
            window.setOpacity(Math.max(0f, Math.min(1f, opacity)));
        } catch (UnsupportedOperationException | IllegalArgumentException ignored) {
            // The panel itself still has its glass finish when window opacity is not available.
        }
    }

    private static float easeOut(float t) {
        float clamped = Math.max(0f, Math.min(1f, t));
        return 1f - (1f - clamped) * (1f - clamped) * (1f - clamped);
    }

    private static String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
