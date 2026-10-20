package ui;

import ui.CinemaGateway.Seat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/** BookMyShow-style seat map: curved screen, available / selected / sold. */
public class SeatGridPanel extends JPanel {

    private final JPanel grid = new JPanel(new GridBagLayout());
    private final List<SeatButton> buttons = new ArrayList<>();
    private final Consumer<List<Seat>> onChange;

    public SeatGridPanel(Consumer<List<Seat>> onChange) {
        this.onChange = onChange;
        setLayout(new BorderLayout(0, 8));
        setBackground(Color.WHITE);

        JPanel screen = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xD0D0D0));
                g2.fillRoundRect(40, 8, getWidth() - 80, 10, 10, 10);
                g2.setColor(UiTheme.MUTED);
                g2.setFont(UiTheme.font(Font.PLAIN, 11));
                String text = "SCREEN THIS WAY";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, 32);
                g2.dispose();
            }
        };
        screen.setPreferredSize(new Dimension(0, 42));
        screen.setOpaque(false);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 4));
        legend.setOpaque(false);
        legend.add(legendItem("Available", Color.WHITE, UiTheme.SEAT_BORDER));
        legend.add(legendItem("Selected", UiTheme.SEAT_SELECTED, UiTheme.SEAT_SELECTED));
        legend.add(legendItem("Sold", UiTheme.SEAT_SOLD, UiTheme.SEAT_SOLD));

        grid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(wrap(grid));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);

        add(screen, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(legend, BorderLayout.SOUTH);
    }

    private static JPanel wrap(JPanel inner) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.add(inner);
        return panel;
    }

    private static JPanel legendItem(String text, Color fill, Color border) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        JLabel box = new JLabel("   ");
        box.setOpaque(true);
        box.setBackground(fill);
        box.setBorder(javax.swing.BorderFactory.createLineBorder(border));
        panel.add(box);
        JLabel label = new JLabel(text);
        label.setForeground(UiTheme.MUTED);
        label.setFont(UiTheme.font(Font.PLAIN, 12));
        panel.add(label);
        return panel;
    }

    public void setSeats(List<Seat> seats) {
        grid.removeAll();
        buttons.clear();

        Map<String, List<Seat>> byRow = new TreeMap<>();
        for (Seat seat : seats) byRow.computeIfAbsent(seat.row(), key -> new ArrayList<>()).add(seat);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 3, 3, 3);
        int y = 0;
        String lastCategory = "";
        for (Map.Entry<String, List<Seat>> entry : byRow.entrySet()) {
            List<Seat> rowSeats = entry.getValue();
            rowSeats.sort(Comparator.comparingInt(Seat::number));
            String category = rowSeats.get(0).category();
            if (!category.equals(lastCategory)) {
                gc.gridy = y++;
                gc.gridx = 0;
                gc.gridwidth = 20;
                gc.insets = new Insets(14, 3, 6, 3);
                JLabel band = new JLabel(category.toUpperCase() + "  —  Rs " + rowSeats.get(0).price(),
                        SwingConstants.CENTER);
                band.setForeground(UiTheme.MUTED);
                band.setFont(UiTheme.font(Font.PLAIN, 11));
                grid.add(band, gc);
                lastCategory = category;
                gc.gridwidth = 1;
                gc.insets = new Insets(3, 3, 3, 3);
            }

            gc.gridy = y++;
            gc.gridx = 0;
            JLabel rowLabel = new JLabel(entry.getKey(), SwingConstants.CENTER);
            rowLabel.setFont(UiTheme.font(Font.BOLD, 12));
            rowLabel.setForeground(UiTheme.MUTED);
            grid.add(rowLabel, gc);
            int x = 1;
            for (Seat seat : rowSeats) {
                if (seat.number() == 6) x++;
                gc.gridx = x++;
                SeatButton button = new SeatButton(seat);
                button.addActionListener(event -> onChange.accept(getSelectedSeats()));
                buttons.add(button);
                grid.add(button, gc);
            }
        }
        grid.revalidate();
        grid.repaint();
        onChange.accept(getSelectedSeats());
    }

    public List<Seat> getSelectedSeats() {
        List<Seat> out = new ArrayList<>();
        for (SeatButton button : buttons) if (button.isSelected()) out.add(button.seat);
        return out;
    }

    private static class SeatButton extends JToggleButton {
        final Seat seat;

        SeatButton(Seat seat) {
            super(String.valueOf(seat.number()));
            this.seat = seat;
            setPreferredSize(new Dimension(34, 30));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(UiTheme.font(Font.PLAIN, 10));
            setEnabled(!seat.booked());
            setToolTipText(seat.label() + "  |  " + seat.category() + "  |  Rs " + seat.price()
                    + (seat.booked() ? "  |  Sold" : ""));
            if (!seat.booked()) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = seat.booked() ? UiTheme.SEAT_SOLD : isSelected() ? UiTheme.SEAT_SELECTED : Color.WHITE;
            Color border = seat.booked() ? UiTheme.SEAT_SOLD : isSelected() ? UiTheme.SEAT_SELECTED : UiTheme.SEAT_BORDER;
            g2.setColor(fill);
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 6, 6);
            g2.setColor(border);
            g2.drawRoundRect(1, 1, getWidth() - 4, getHeight() - 4, 6, 6);
            g2.setColor(isSelected() || seat.booked() ? Color.WHITE : UiTheme.TEXT);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}
