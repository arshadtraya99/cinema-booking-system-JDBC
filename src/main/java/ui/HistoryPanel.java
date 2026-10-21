package ui;

import ui.CinemaGateway.Booking;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class HistoryPanel extends JPanel {

    private final CinemaGateway gateway;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Booking #", "Movie", "Seat", "Customer", "Issued by", "Amount (Rs)", "Booked at"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel revenue = UiTheme.heading("", 14);

    public HistoryPanel(CinemaGateway gateway) {
        this.gateway = gateway;
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);
        UiTheme.pad(this, 22, 26, 22, 26);

        JLabel title = UiTheme.heading("Booking history", 26);
        JLabel sub = UiTheme.muted("Every confirmed ticket, with a running box-office total.");

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new javax.swing.BoxLayout(titles, javax.swing.BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(javax.swing.Box.createVerticalStrut(7));
        titles.add(sub);
        heading.add(titles, BorderLayout.WEST);

        JTable table = new JTable(model);
        UiTheme.styleTable(table);
        table.setAutoCreateRowSorter(true);

        JButton reload = UiTheme.darkButton("Refresh");
        reload.addActionListener(e -> refresh());

        JPanel bottom = UiTheme.glassPanel(new BorderLayout());
        bottom.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(255, 255, 255, 28), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        bottom.add(revenue, BorderLayout.WEST);
        bottom.add(reload, BorderLayout.EAST);

        add(heading, BorderLayout.NORTH);
        add(UiTheme.scroll(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");
        for (Booking booking : gateway.history()) {
            model.addRow(new Object[]{
                    booking.id(), booking.movieTitle(), booking.seatLabel(), booking.customerName(),
                    booking.clerkName(), booking.amount(), booking.bookedAt().format(formatter)
            });
            total = total.add(booking.amount());
        }
        revenue.setText("Box office  ·  Rs " + total + "   ·   " + model.getRowCount() + " tickets");
    }
}
