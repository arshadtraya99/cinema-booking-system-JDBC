package ui;

import ui.CinemaGateway.User;

import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
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

        BookingPanel booking = new BookingPanel(gateway, user);
        HistoryPanel history = new HistoryPanel(gateway);
        AdminPanel admin = user.isAdmin() ? new AdminPanel(gateway) : null;

        CardLayout contentLayout = new CardLayout();
        JPanel content = new JPanel(contentLayout);
        content.add(booking, "films");
        content.add(history, "bookings");
        if (admin != null) content.add(admin, "manage");

        JToggleButton filmsButton = navButton("01   FILMS", true);
        JToggleButton bookingsButton = navButton("02   BOOKINGS", false);
        ButtonGroup navGroup = new ButtonGroup();
        navGroup.add(filmsButton);
        navGroup.add(bookingsButton);
        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navigation.setBackground(UiTheme.NAVY);
        navigation.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
        navigation.setPreferredSize(new Dimension(0, 48));
        navigation.add(filmsButton);
        navigation.add(bookingsButton);

        filmsButton.addActionListener(e -> {
            contentLayout.show(content, "films");
            booking.refresh();
        });
        bookingsButton.addActionListener(e -> {
            contentLayout.show(content, "bookings");
            history.refresh();
        });
        if (admin != null) {
            JToggleButton manageButton = navButton("03   MANAGE", false);
            navGroup.add(manageButton);
            navigation.add(manageButton);
            manageButton.addActionListener(e -> {
                contentLayout.show(content, "manage");
                admin.refresh();
            });
        }

        JPanel top = new JPanel(new BorderLayout());
        top.add(header(gateway, user), BorderLayout.NORTH);
        top.add(navigation, BorderLayout.SOUTH);
        shell.add(top, BorderLayout.NORTH);
        shell.add(content, BorderLayout.CENTER);
        setContentPane(shell);
    }

    private JToggleButton navButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text, selected);
        button.setFont(UiTheme.font(Font.BOLD, 11));
        button.setForeground(selected ? UiTheme.TEXT : UiTheme.MUTED);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, selected ? 2 : 0, 0,
                        selected ? UiTheme.PINK : UiTheme.NAVY),
                BorderFactory.createEmptyBorder(12, 14, 10, 14)));
        button.addItemListener(e -> {
            boolean active = button.isSelected();
            button.setForeground(active ? UiTheme.TEXT : UiTheme.MUTED);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, active ? 2 : 0, 0,
                            active ? UiTheme.PINK : UiTheme.NAVY),
                    BorderFactory.createEmptyBorder(12, 14, 10, 14)));
        });
        return button;
    }

    private JPanel header(CinemaGateway gateway, User user) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UiTheme.NAVY);
        bar.setPreferredSize(new Dimension(0, 72));
        UiTheme.pad(bar, 0, 22, 0, 16);

        JLabel mark = new JLabel("C");
        mark.setOpaque(true);
        mark.setBackground(UiTheme.PINK);
        mark.setForeground(Color.WHITE);
        mark.setHorizontalAlignment(JLabel.CENTER);
        mark.setFont(UiTheme.font(Font.BOLD, 17));
        mark.setPreferredSize(new Dimension(34, 34));
        mark.setBorder(BorderFactory.createLineBorder(UiTheme.PINK, 1, true));

        JLabel brand = new JLabel("CINEBOOK");
        brand.setForeground(Color.WHITE);
        brand.setFont(UiTheme.font(Font.BOLD, 18));
        JLabel tag = new JLabel("  THE MOVIE NIGHT CLUB");
        tag.setForeground(UiTheme.MUTED);
        tag.setFont(UiTheme.font(Font.BOLD, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 18));
        left.setOpaque(false);
        left.add(mark);
        left.add(brand);
        left.add(tag);

        JLabel who = new JLabel(user.name() + "  ·  " + user.role() + "   ");
        who.setForeground(UiTheme.MUTED);
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
