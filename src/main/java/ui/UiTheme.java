package ui;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** BookMyShow / District-inspired palette and shared Swing styling. */
public final class UiTheme {

    public static final Color PINK = new Color(0xF84464);
    public static final Color PINK_DARK = new Color(0xD43754);
    public static final Color NAVY = new Color(0x1F2533);
    public static final Color NAVY_SOFT = new Color(0x2B3142);
    public static final Color PAGE = new Color(0xF2F2F5);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x222222);
    public static final Color MUTED = new Color(0x6B6B6B);
    public static final Color LINE = new Color(0xE6E6EA);
    public static final Color SHOW_GREEN = new Color(0x49BA8E);
    public static final Color SEAT_SELECTED = new Color(0x1EA83C);
    public static final Color SEAT_SOLD = new Color(0xBBBBBB);
    public static final Color SEAT_BORDER = new Color(0xC8C8C8);

    private UiTheme() {}

    public static Font font(int style, float size) {
        return new Font("SansSerif", style, Math.round(size));
    }

    public static void applyDefaults() {
        UIManager.put("Label.font", font(Font.PLAIN, 13));
        UIManager.put("Button.font", font(Font.BOLD, 13));
        UIManager.put("TextField.font", font(Font.PLAIN, 13));
        UIManager.put("PasswordField.font", font(Font.PLAIN, 13));
        UIManager.put("ComboBox.font", font(Font.PLAIN, 13));
        UIManager.put("Table.font", font(Font.PLAIN, 13));
        UIManager.put("TabbedPane.font", font(Font.BOLD, 13));
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 13));
    }

    public static JButton primaryButton(String text) {
        return roundedButton(text, PINK, Color.WHITE, PINK_DARK);
    }

    public static JButton darkButton(String text) {
        return roundedButton(text, NAVY, Color.WHITE, NAVY_SOFT);
    }

    public static JButton ghostButton(String text) {
        JButton button = roundedButton(text, CARD, TEXT, LINE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(8, 14, 8, 14)));
        return button;
    }

    public static JButton roundedButton(String text, Color bg, Color fg, Color hover) {
        JButton button = new JButton(text) {
            private boolean hovered;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered && isEnabled() ? hover : (isEnabled() ? bg : LINE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
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
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        return button;
    }

    public static void styleField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        field.setBackground(CARD);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setFont(font(Font.PLAIN, 13));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(0xFDE8EC));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(font(Font.BOLD, 12));
        header.setBackground(new Color(0xFAFAFA));
        header.setForeground(MUTED);
        header.setReorderingAllowed(false);
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createLineBorder(LINE));
        pane.getViewport().setBackground(CARD);
        pane.setBackground(PAGE);
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
                new Color(0x2B1B3D), new Color(0x1B3A4B), new Color(0x3D1F2B),
                new Color(0x1F3D2B), new Color(0x3D2B1F), new Color(0x222244)
        };
        int h = seed == null ? 0 : seed.hashCode();
        return palette[Math.abs(h) % palette.length];
    }

    public static void flatten(AbstractButton button) {
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(6, 12, 6, 12));
    }
}
