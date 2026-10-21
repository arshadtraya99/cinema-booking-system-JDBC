package ui;

import ui.CinemaGateway.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Main application shell: persistent navigation, liquid-glass surfaces, and animated page changes. */
public class MainFrame extends JFrame {

    private final AnimatedCardPanel content;
    private final JLabel pageTitle = UiTheme.heading("Discover movies", 22);

    public MainFrame(CinemaGateway gateway, User user) {
        super("Book Your Show — Cinema Workspace");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int frameWidth = Math.min(1440, Math.max(1000, screen.width - 48));
        int frameHeight = Math.min(900, Math.max(640, screen.height - 72));
        setSize(frameWidth, frameHeight);
        setMinimumSize(new Dimension(Math.min(1120, frameWidth), Math.min(680, frameHeight)));
        setLocationRelativeTo(null);

        JPanel shell = UiTheme.gradientPanel(new BorderLayout(18, 0));
        UiTheme.pad(shell, 16, 16, 16, 16);

        BookingPanel booking = new BookingPanel(gateway, user);
        HistoryPanel history = new HistoryPanel(gateway);
        AdminPanel admin = user.isAdmin() ? new AdminPanel(gateway) : null;

        CardLayout contentLayout = new CardLayout();
        content = new AnimatedCardPanel(contentLayout);
        content.add(booking, "films");
        content.add(history, "bookings");
        if (admin != null) content.add(admin, "manage");

        JToggleButton filmsButton = navButton("▣    Discover movies", true);
        JToggleButton bookingsButton = navButton("▤    Booking history", false);
        JToggleButton manageButton = admin == null ? null : navButton("✦    Admin studio", false);
        ButtonGroup navGroup = new ButtonGroup();
        navGroup.add(filmsButton);
        navGroup.add(bookingsButton);
        if (manageButton != null) navGroup.add(manageButton);

        JPanel sidebar = buildSidebar(user, filmsButton, bookingsButton, manageButton, gateway);
        JPanel workspace = new JPanel(new BorderLayout(0, 16));
        workspace.setOpaque(false);
        workspace.add(buildTopbar(gateway, user), BorderLayout.NORTH);
        workspace.add(content, BorderLayout.CENTER);

        filmsButton.addActionListener(e -> navigate("films", "Discover movies", booking));
        bookingsButton.addActionListener(e -> navigate("bookings", "Booking history", history));
        if (manageButton != null) {
            manageButton.addActionListener(e -> navigate("manage", "Admin studio", admin));
        }

        shell.add(sidebar, BorderLayout.WEST);
        shell.add(workspace, BorderLayout.CENTER);
        setContentPane(shell);
    }

    private void navigate(String key, String title, Object panel) {
        pageTitle.setText(title);
        content.showCard(key);
        if (panel instanceof BookingPanel booking) booking.refresh();
        else if (panel instanceof HistoryPanel history) history.refresh();
        else if (panel instanceof AdminPanel admin) admin.refresh();
    }

    private JPanel buildSidebar(User user, JToggleButton films, JToggleButton bookings,
                                JToggleButton manage, CinemaGateway gateway) {
        JPanel sidebar = UiTheme.glassPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(242, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 235, 255, 38), 1, true),
                BorderFactory.createEmptyBorder(20, 15, 16, 15)));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(new BrandLogo(42));
        JPanel wordmark = new JPanel();
        wordmark.setOpaque(false);
        wordmark.setLayout(new BoxLayout(wordmark, BoxLayout.Y_AXIS));
        JLabel brandName = UiTheme.heading("SHOWTIME", 17);
        JLabel brandTag = UiTheme.muted("CINEMA WORKSPACE");
        brandTag.setFont(UiTheme.font(Font.BOLD, 9));
        wordmark.add(Box.createVerticalStrut(2));
        wordmark.add(brandName);
        wordmark.add(Box.createVerticalStrut(3));
        wordmark.add(brandTag);
        brand.add(wordmark);

        JPanel divider = new JPanel();
        divider.setOpaque(true);
        divider.setBackground(new Color(210, 225, 255, 25));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setPreferredSize(new Dimension(1, 1));

        JLabel sectionLabel = UiTheme.muted("WORKSPACE");
        sectionLabel.setFont(UiTheme.font(Font.BOLD, 10));
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(18, 7, 10, 0));
        sectionLabel.setAlignmentX(LEFT_ALIGNMENT);

        stack.add(brand);
        stack.add(Box.createVerticalStrut(25));
        stack.add(divider);
        stack.add(sectionLabel);
        stack.add(films);
        stack.add(Box.createVerticalStrut(7));
        stack.add(bookings);
        if (manage != null) {
            stack.add(Box.createVerticalStrut(7));
            stack.add(manage);
        }
        stack.add(Box.createVerticalGlue());

        JPanel profile = UiTheme.glassPanel(new BorderLayout(10, 0));
        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 235, 255, 28), 1, true),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));
        JPanel avatar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new java.awt.GradientPaint(0, 0, UiTheme.PINK, getWidth(), getHeight(), UiTheme.PURPLE));
                g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setMinimumSize(new Dimension(38, 38));
        avatar.add(new JLabel(initials(user.name()), SwingConstants.CENTER) {{
            setForeground(Color.WHITE);
            setFont(UiTheme.font(Font.BOLD, 13));
        }});

        JPanel userLabels = new JPanel();
        userLabels.setOpaque(false);
        userLabels.setLayout(new BoxLayout(userLabels, BoxLayout.Y_AXIS));
        JLabel name = UiTheme.heading(user.name(), 12);
        JLabel role = UiTheme.muted(user.role().toUpperCase());
        role.setFont(UiTheme.font(Font.BOLD, 9));
        userLabels.add(name);
        userLabels.add(Box.createVerticalStrut(4));
        userLabels.add(role);
        profile.add(avatar, BorderLayout.WEST);
        profile.add(userLabels, BorderLayout.CENTER);
        profile.setAlignmentX(LEFT_ALIGNMENT);
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        stack.add(Box.createVerticalStrut(18));
        stack.add(profile);
        sidebar.add(stack, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildTopbar(CinemaGateway gateway, User user) {
        JPanel bar = UiTheme.glassPanel(new BorderLayout(16, 0));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 235, 255, 38), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 18)));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel eyebrow = UiTheme.muted("BOOK YOUR SHOW   /   WORKSPACE");
        eyebrow.setFont(UiTheme.font(Font.BOLD, 9));
        heading.add(eyebrow);
        heading.add(Box.createVerticalStrut(4));
        heading.add(pageTitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 11, 4));
        actions.setOpaque(false);
        boolean databaseConnected = gateway instanceof JdbcCinemaGateway;
        JLabel status = new JLabel(databaseConnected ? "●  DATABASE CONNECTED" : "●  DEMO MODE");
        status.setForeground(databaseConnected ? UiTheme.MINT : new Color(0xFBBF24));
        status.setFont(UiTheme.font(Font.BOLD, 10));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(status.getForeground().getRed(),
                        status.getForeground().getGreen(), status.getForeground().getBlue(), 75), 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        JLabel who = new JLabel(user.name());
        who.setForeground(UiTheme.MUTED);
        who.setFont(UiTheme.font(Font.BOLD, 12));
        JButton logout = UiTheme.ghostButton("Log out  ↗");
        logout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 235, 255, 32), 1, true),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame(gateway).setVisible(true);
        });
        actions.add(status);
        actions.add(who);
        actions.add(logout);
        bar.add(heading, BorderLayout.WEST);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JToggleButton navButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text, selected) {
            private boolean hovered;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected() || hovered) {
                    Color top = isSelected() ? new Color(70, 87, 145, 92) : new Color(255, 255, 255, 12);
                    Color bottom = isSelected() ? new Color(54, 103, 161, 56) : new Color(255, 255, 255, 4);
                    g.setPaint(new java.awt.GradientPaint(0, 0, top, getWidth(), getHeight(), bottom));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                }
                if (isSelected()) {
                    g.setPaint(new java.awt.GradientPaint(0, 7, UiTheme.MINT, 0, getHeight() - 7, UiTheme.PINK));
                    g.fillRoundRect(0, 9, 3, Math.max(1, getHeight() - 18), 3, 3);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(UiTheme.font(Font.BOLD, 12));
        button.setForeground(selected ? UiTheme.TEXT : UiTheme.MUTED);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        button.setPreferredSize(new Dimension(206, 48));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.addItemListener(e -> button.setForeground(button.isSelected() ? UiTheme.TEXT : UiTheme.MUTED));
        return button;
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] pieces = name.trim().split("\\s+");
        if (pieces.length == 1) return pieces[0].substring(0, Math.min(2, pieces[0].length())).toUpperCase();
        return (pieces[0].charAt(0) + "" + pieces[pieces.length - 1].charAt(0)).toUpperCase();
    }
}
