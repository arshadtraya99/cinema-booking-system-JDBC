package ui;

import ui.CinemaGateway.User;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public class MainFrame extends JFrame {

    public MainFrame(CinemaGateway gateway, User user) {
        super("CineBook");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(980, 640));

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(UiTheme.PAGE);
        shell.add(header(gateway, user), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiTheme.font(Font.BOLD, 13));
        tabs.setBackground(UiTheme.PAGE);
        tabs.setForeground(UiTheme.TEXT);

        BookingPanel booking = new BookingPanel(gateway, user);
        HistoryPanel history = new HistoryPanel(gateway);
        AdminPanel admin = user.isAdmin() ? new AdminPanel(gateway) : null;

        tabs.addTab("Now showing", booking);
        tabs.addTab("Bookings", history);
        if (admin != null) tabs.addTab("Manage", admin);

        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == booking) booking.refresh();
            else if (selected == history) history.refresh();
            else if (admin != null && selected == admin) admin.refresh();
        });

        shell.add(tabs, BorderLayout.CENTER);
        setContentPane(shell);
    }

    private JPanel header(CinemaGateway gateway, User user) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UiTheme.NAVY);
        bar.setPreferredSize(new Dimension(0, 64));
        UiTheme.pad(bar, 0, 18, 0, 12);

        JLabel brand = new JLabel("CineBook");
        brand.setForeground(Color.WHITE);
        brand.setFont(UiTheme.font(Font.BOLD, 22));
        JLabel tag = new JLabel("  movies  ·  theatres  ·  seats");
        tag.setForeground(new Color(0xC9CDD6));
        tag.setFont(UiTheme.font(Font.PLAIN, 13));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        left.setOpaque(false);
        left.add(brand);
        left.add(tag);

        JLabel who = new JLabel(user.name() + "  ·  " + user.role() + "   ");
        who.setForeground(Color.WHITE);
        who.setFont(UiTheme.font(Font.PLAIN, 13));

        JButton logout = UiTheme.primaryButton("Logout");
        logout.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame(gateway).setVisible(true);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        right.setOpaque(false);
        right.add(who);
        right.add(logout);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }
}
