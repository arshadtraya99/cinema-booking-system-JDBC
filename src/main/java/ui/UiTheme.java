package ui;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.RadialGradientPaint;

/** Shared liquid-glass design system for the Book Your Show desktop app. */
public final class UiTheme {

    // Kept under the original names so existing panels can use the theme unchanged.
    public static final Color PINK = new Color(0x8B5CF6);       // electric violet
    public static final Color PINK_DARK = new Color(0x6D28D9);
    public static final Color PURPLE = new Color(0x3B82F6);     // vivid blue
    public static final Color NAVY = new Color(0x070B16);
    public static final Color NAVY_SOFT = new Color(0x111A2D);
    public static final Color PAGE = new Color(0x0A1020);
    public static final Color CARD = new Color(0x111A2D);
    public static final Color CARD_RAISED = new Color(0x1A2943);
    public static final Color TEXT = new Color(0xF5F7FF);
    public static final Color MUTED = new Color(0x9AAAC5);
    public static final Color LINE = new Color(0x33405B);
    public static final Color SHOW_GREEN = new Color(0x67E8F9);
    public static final Color SEAT_SELECTED = new Color(0x22D3EE);
    public static final Color SEAT_SOLD = new Color(0x29344A);
    public static final Color SEAT_BORDER = new Color(0x3C5275);
    public static final Color MINT = new Color(0x55D6FF);

    private UiTheme() { }

    public static Font font(int style, float size) {
        return new Font("SansSerif", style, Math.round(size));
    }

    public static void applyDefaults() {
        UIManager.put("Label.font", font(Font.PLAIN, 13));
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Panel.background", PAGE);
        UIManager.put("Button.font", font(Font.BOLD, 13));
        UIManager.put("Button.background", CARD_RAISED);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("ToggleButton.font", font(Font.BOLD, 13));
        UIManager.put("ToggleButton.background", CARD_RAISED);
        UIManager.put("ToggleButton.foreground", TEXT);
        UIManager.put("TextField.font", font(Font.PLAIN, 13));
        UIManager.put("TextField.background", CARD_RAISED);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("PasswordField.font", font(Font.PLAIN, 13));
        UIManager.put("PasswordField.background", CARD_RAISED);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("ComboBox.font", font(Font.PLAIN, 13));
        UIManager.put("ComboBox.background", CARD_RAISED);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", PINK_DARK);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Table.font", font(Font.PLAIN, 13));
        UIManager.put("Table.background", CARD);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", new Color(0x243B62));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.background", CARD_RAISED);
        UIManager.put("TableHeader.foreground", MUTED);
        UIManager.put("TabbedPane.font", font(Font.BOLD, 13));
        UIManager.put("TabbedPane.background", PAGE);
        UIManager.put("TabbedPane.foreground", MUTED);
        UIManager.put("TabbedPane.selected", CARD);
        UIManager.put("ScrollPane.background", PAGE);
        UIManager.put("Viewport.background", PAGE);
        UIManager.put("ScrollBar.background", PAGE);
        UIManager.put("ScrollBar.track", PAGE);
        UIManager.put("ScrollBar.thumb", CARD_RAISED);
        UIManager.put("ScrollBar.thumbDarkShadow", LINE);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.buttonBackground", CARD_RAISED);
        UIManager.put("OptionPane.buttonForeground", TEXT);
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 13));
    }

    public static JButton primaryButton(String text) {
        return roundedButton(text, PINK, Color.WHITE, PURPLE);
    }

    public static JButton darkButton(String text) {
        return roundedButton(text, NAVY_SOFT, Color.WHITE, CARD_RAISED);
    }

    public static JButton ghostButton(String text) {
        JButton button = roundedButton(text, CARD_RAISED, TEXT, new Color(0x263857));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(170, 190, 235, 45), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        return button;
    }

    /** A rounded gradient button with a short animated glow on hover. */
    public static JButton roundedButton(String text, Color bg, Color fg, Color hover) {
        JButton button = new JButton(text) {
            private float hoverProgress;
            private float targetProgress;
            private final Timer hoverTimer = new Timer(16, event -> {
                float delta = targetProgress - hoverProgress;
                if (Math.abs(delta) < 0.035f) {
                    hoverProgress = targetProgress;
                    ((Timer) event.getSource()).stop();
                } else {
                    hoverProgress += delta * 0.34f;
                }
                repaint();
            });

            {
                hoverTimer.setCoalesce(true);
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        if (!isEnabled()) return;
                        targetProgress = 1f;
                        if (!hoverTimer.isRunning()) hoverTimer.start();
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        targetProgress = 0f;
                        if (!hoverTimer.isRunning()) hoverTimer.start();
                    }
                });
                addPropertyChangeListener("enabled", event -> {
                    if (!isEnabled()) {
                        targetProgress = 0f;
                        hoverProgress = 0f;
                        hoverTimer.stop();
                        repaint();
                    }
                });
            }

            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                if (w <= 2 || h <= 2) {
                    g.dispose();
                    super.paintComponent(graphics);
                    return;
                }
                Color base = isEnabled() ? mix(bg, hover, hoverProgress * 0.8f) : LINE;
                if (hoverProgress > 0.01f) {
                    g.setColor(withAlpha(hover, Math.round(52 * hoverProgress)));
                    g.setStroke(new BasicStroke(8f + 4f * hoverProgress, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));
                    g.drawRoundRect(4, 4, w - 9, h - 9, 18, 18);
                }
                g.setPaint(new java.awt.GradientPaint(0, 0, mix(base, Color.WHITE, 0.12f),
                        0, h, mix(base, NAVY, 0.14f)));
                g.fillRoundRect(0, 0, w, h, 15, 15);
                g.setPaint(new java.awt.GradientPaint(0, 1, new Color(255, 255, 255, 38),
                        0, Math.max(2, h / 2), new Color(255, 255, 255, 0)));
                g.fillRoundRect(1, 1, w - 2, Math.max(1, h / 2), 14, 14);
                g.setColor(new Color(255, 255, 255, 45 + Math.round(25 * hoverProgress)));
                g.setStroke(new BasicStroke(1f));
                g.drawRoundRect(0, 0, w - 1, h - 1, 15, 15);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setUI(new BasicButtonUI());
        button.setForeground(fg);
        button.setFont(font(Font.BOLD, 13));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 20, 11, 20));
        button.getAccessibleContext().setAccessibleName(text);
        return button;
    }

    /** Animated midnight backdrop with slow-moving violet and cyan light blooms. */
    public static JPanel gradientPanel(LayoutManager layout) {
        return new AnimatedGradientPanel(layout);
    }

    /** Translucent layered panel with an inner reflection and a responsive edge glow. */
    public static JPanel glassPanel(LayoutManager layout) {
        return new LiquidGlassPanel(layout);
    }

    private static final class AnimatedGradientPanel extends JPanel {
        private double phase;
        private final Timer animation = new Timer(42, e -> {
            phase += 0.008;
            if (phase > Math.PI * 2) phase -= Math.PI * 2;
            repaint();
        });

        AnimatedGradientPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            animation.setCoalesce(true);
        }

        @Override public void addNotify() {
            super.addNotify();
            if (!animation.isRunning()) animation.start();
        }

        @Override public void removeNotify() {
            animation.stop();
            super.removeNotify();
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                g.dispose();
                super.paintComponent(graphics);
                return;
            }
            g.setPaint(new java.awt.GradientPaint(0, 0, new Color(0x0C1428),
                    w, h, new Color(0x070B16)));
            g.fillRect(0, 0, w, h);

            float x1 = (float) (w * (0.18 + 0.09 * Math.sin(phase)));
            float y1 = (float) (h * (0.18 + 0.08 * Math.cos(phase * 0.83)));
            float r1 = Math.max(1, Math.max(w, h) * 0.55f);
            Paint violetBloom = new RadialGradientPaint(x1, y1, r1,
                    new float[]{0f, 0.48f, 1f},
                    new Color[]{new Color(91, 63, 183, 66), new Color(70, 60, 150, 23),
                            new Color(70, 60, 150, 0)});
            g.setPaint(violetBloom);
            g.fill(new Ellipse2D.Double(x1 - r1, y1 - r1, r1 * 2, r1 * 2));

            float x2 = (float) (w * (0.88 + 0.06 * Math.cos(phase * 0.72)));
            float y2 = (float) (h * (0.76 + 0.08 * Math.sin(phase * 0.92)));
            float r2 = Math.max(1, Math.max(w, h) * 0.47f);
            g.setPaint(new RadialGradientPaint(x2, y2, r2,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{new Color(23, 174, 219, 46), new Color(28, 105, 175, 18),
                            new Color(28, 105, 175, 0)}));
            g.fill(new Ellipse2D.Double(x2 - r2, y2 - r2, r2 * 2, r2 * 2));

            // Very subtle top-edge reflection, a hallmark of the glass treatment.
            g.setPaint(new java.awt.GradientPaint(0, 0, new Color(255, 255, 255, 13),
                    0, 120, new Color(255, 255, 255, 0)));
            g.fillRect(0, 0, w, Math.min(h, 120));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class LiquidGlassPanel extends JPanel {
        private float hoverProgress;
        private float targetProgress;
        private double sheenPhase;
        private final Timer animation = new Timer(16, e -> {
            float delta = targetProgress - hoverProgress;
            if (Math.abs(delta) < 0.025f) {
                hoverProgress = targetProgress;
                if (hoverProgress <= 0.01f) ((Timer) e.getSource()).stop();
            } else {
                hoverProgress += delta * 0.28f;
            }
            sheenPhase += 0.025;
            if (sheenPhase > 1.3) sheenPhase = -0.3;
            repaint();
        });

        LiquidGlassPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            animation.setCoalesce(true);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    targetProgress = 1f;
                    if (!animation.isRunning()) animation.start();
                }
                @Override public void mouseExited(MouseEvent e) {
                    targetProgress = 0f;
                    if (!animation.isRunning()) animation.start();
                }
            });
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (w <= 2 || h <= 2) {
                g.dispose();
                super.paintComponent(graphics);
                return;
            }
            RoundRectangle2D shape = new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 24, 24);
            if (hoverProgress > 0.01f || Boolean.TRUE.equals(getClientProperty("hoverGlow"))) {
                float glow = Math.max(hoverProgress, 0.35f);
                g.setColor(new Color(PURPLE.getRed(), PURPLE.getGreen(), PURPLE.getBlue(),
                        Math.round(24 * glow)));
                g.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawRoundRect(7, 7, Math.max(0, w - 15), Math.max(0, h - 15), 25, 25);
                g.setColor(new Color(MINT.getRed(), MINT.getGreen(), MINT.getBlue(),
                        Math.round(15 * glow)));
                g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawRoundRect(4, 4, Math.max(0, w - 9), Math.max(0, h - 9), 23, 23);
            }
            g.setPaint(new java.awt.GradientPaint(0, 0, new Color(27, 40, 65, 232),
                    w, h, new Color(12, 20, 37, 218)));
            g.fill(shape);

            // Soft top reflection: brighter at the top, fading before the content begins.
            g.setPaint(new java.awt.GradientPaint(0, 2, new Color(255, 255, 255, 31),
                    0, Math.max(20, h * 0.42f), new Color(255, 255, 255, 0)));
            g.fill(new RoundRectangle2D.Float(2, 2, w - 5, Math.max(1, h * 0.48f), 22, 22));

            // A moving, translucent sheen is only animated while the pointer is over the panel.
            if (hoverProgress > 0.02f) {
                float sheenX = (float) ((w + 180) * sheenPhase - 90);
                g.setPaint(new java.awt.GradientPaint(sheenX, 0, new Color(255, 255, 255, 0),
                        sheenX + 90, 0, new Color(190, 220, 255, Math.round(12 * hoverProgress))));
                g.fill(shape);
            }
            g.setColor(new Color(220, 235, 255, 42 + Math.round(20 * hoverProgress)));
            g.setStroke(new BasicStroke(1f));
            g.draw(shape);
            g.setColor(new Color(255, 255, 255, 12));
            g.drawRoundRect(3, 3, Math.max(0, w - 7), Math.max(0, h - 7), 21, 21);
            g.dispose();
            super.paintComponent(graphics);
        }

        @Override public void removeNotify() {
            animation.stop();
            super.removeNotify();
        }
    }

    public static <T> void centerComboBox(JComboBox<T> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(isSelected ? Color.WHITE : TEXT);
                label.setBackground(isSelected ? PINK_DARK : CARD_RAISED);
                label.setBorder(new EmptyBorder(5, 8, 5, 8));
                return label;
            }
        });
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(105, 135, 190, 105), 1, true),
                new EmptyBorder(2, 5, 2, 5)));
        combo.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) {
                combo.setBorder(BorderFactory.createLineBorder(new Color(MINT.getRed(), MINT.getGreen(),
                        MINT.getBlue(), 160), 1, true));
            }
            @Override public void mouseExited(MouseEvent event) {
                combo.setBorder(BorderFactory.createLineBorder(new Color(105, 135, 190, 105), 1, true));
            }
        });
    }

    public static void styleField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 210, 255, 42), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        field.setBackground(new Color(13, 22, 40));
        field.setForeground(TEXT);
        field.setCaretColor(MINT);
        field.setSelectionColor(new Color(60, 100, 170, 180));
        field.setSelectedTextColor(TEXT);
        field.setFont(font(Font.PLAIN, 13));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(0x243B62));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(255, 255, 255, 8));
        table.setOpaque(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(font(Font.BOLD, 12));
        header.setBackground(CARD_RAISED);
        header.setForeground(MUTED);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setOpaque(true);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        source, value, false, false, row, column);
                label.setOpaque(true);
                label.setBackground(CARD_RAISED);
                label.setForeground(MUTED);
                label.setFont(font(Font.BOLD, 11));
                label.setHorizontalAlignment(JLabel.LEFT);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(200, 220, 255, 16)),
                        new EmptyBorder(0, 12, 0, 8)));
                return label;
            }
        });
    }

    /** Removes the default bright Metal tab outlines and paints understated glass-style tabs. */
    public static void styleTabbedPane(JTabbedPane tabs) {
        tabs.setOpaque(false);
        tabs.setFont(font(Font.BOLD, 12));
        tabs.setForeground(TEXT);
        tabs.setBackground(PAGE);
        tabs.setBorder(BorderFactory.createEmptyBorder());
        tabs.setUI(new BasicTabbedPaneUI() {
            {
                tabInsets = new Insets(8, 12, 8, 12);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                contentBorderInsets = new Insets(7, 7, 7, 7);
            }

            @Override protected void paintTabBackground(Graphics graphics, int placement, int index,
                    int x, int y, int width, int height, boolean selected) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selected) {
                    g.setPaint(new java.awt.GradientPaint(x, y, PINK,
                            x + width, y + height, PURPLE));
                } else {
                    g.setPaint(new java.awt.GradientPaint(x, y, new Color(29, 42, 64, 205),
                            x, y + height, new Color(13, 22, 39, 190)));
                }
                g.fillRoundRect(x + 1, y + 1, Math.max(0, width - 2), Math.max(0, height - 2), 10, 10);
                g.dispose();
            }

            @Override protected void paintTabBorder(Graphics graphics, int placement, int index,
                    int x, int y, int width, int height, boolean selected) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(selected ? new Color(210, 230, 255, 100) : new Color(190, 210, 245, 27));
                g.drawRoundRect(x + 1, y + 1, Math.max(0, width - 3), Math.max(0, height - 3), 10, 10);
                g.dispose();
            }

            @Override protected void paintContentBorder(Graphics graphics, int placement, int selectedIndex) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int x = 1;
                int y = calculateTabAreaHeight(placement, runCount, maxTabHeight) + 1;
                int width = tabPane.getWidth() - 3;
                int height = tabPane.getHeight() - y - 2;
                if (width > 0 && height > 0) {
                    g.setColor(new Color(200, 220, 255, 28));
                    g.drawRoundRect(x, y, width, height, 16, 16);
                    g.setColor(new Color(255, 255, 255, 8));
                    g.drawRoundRect(x + 2, y + 2, width - 4, height - 4, 14, 14);
                }
                g.dispose();
            }
        });
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setForegroundAt(i, MUTED);
        }
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createLineBorder(new Color(170, 195, 235, 30), 1, true));
        pane.setOpaque(false);
        pane.getViewport().setBackground(CARD);
        pane.setBackground(PAGE);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.getHorizontalScrollBar().setUnitIncrement(18);
        return pane;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(font(Font.PLAIN, 12));
        return label;
    }

    public static JLabel heading(String text, float size) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(font(Font.BOLD, size));
        return label;
    }

    public static void pad(JComponent component, int top, int left, int bottom, int right) {
        component.setBorder(new EmptyBorder(top, left, bottom, right));
    }

    public static Color posterColor(String seed) {
        Color[] palette = {
                new Color(0x25245B), new Color(0x123B4A), new Color(0x4B2455),
                new Color(0x183F3A), new Color(0x493021), new Color(0x202F59)
        };
        int h = seed == null ? 0 : seed.hashCode();
        return palette[Math.floorMod(h, palette.length)];
    }

    public static void flatten(AbstractButton button) {
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(6, 12, 6, 12));
    }

    private static Color mix(Color a, Color b, float amount) {
        float t = Math.max(0f, Math.min(1f, amount));
        float r = 1f - t;
        return new Color(Math.round(a.getRed() * r + b.getRed() * t),
                Math.round(a.getGreen() * r + b.getGreen() * t),
                Math.round(a.getBlue() * r + b.getBlue() * t),
                Math.round(a.getAlpha() * r + b.getAlpha() * t));
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.max(0, Math.min(255, alpha)));
    }
}
