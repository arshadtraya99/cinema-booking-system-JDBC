package ui;

import ui.CinemaGateway.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

public class LoginFrame extends JFrame {

    private final CinemaGateway gateway;
    private final CardLayout cards = new CardLayout();
    private final JPanel formCards = new JPanel(cards);

    public LoginFrame(CinemaGateway gateway) {
        super("CineBook");
        this.gateway = gateway;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.PAGE);
        root.add(brandPanel(), BorderLayout.WEST);
        root.add(formPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel brandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(0x241B2B),
                        getWidth(), getHeight(), UiTheme.NAVY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(UiTheme.PINK.getRed(), UiTheme.PINK.getGreen(),
                        UiTheme.PINK.getBlue(), 24));
                g2.fillOval(getWidth() - 150, -90, 290, 290);
                g2.setColor(new Color(UiTheme.MINT.getRed(), UiTheme.MINT.getGreen(),
                        UiTheme.MINT.getBlue(), 12));
                g2.fillOval(-130, getHeight() - 190, 300, 300);
                g2.setColor(UiTheme.PINK);
                g2.fillRect(0, 0, 5, getHeight());
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(420, 0));

        JLabel kicker = new JLabel("✦  YOUR NEXT FAVORITE NIGHT");
        kicker.setForeground(UiTheme.PINK);
        kicker.setFont(UiTheme.font(Font.BOLD, 12));

        JLabel title = new JLabel("<html>Lights down.<br>Good times on.</html>");
        title.setForeground(Color.WHITE);
        title.setFont(UiTheme.font(Font.BOLD, 34));

        JLabel copy = new JLabel("<html>Find your film, pick the perfect seats, "
                + "and make tonight feel like a premiere.</html>");
        copy.setForeground(UiTheme.MUTED);
        copy.setFont(UiTheme.font(Font.PLAIN, 14));
        copy.setPreferredSize(new Dimension(320, 70));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(8, 40, 8, 40);
        panel.add(kicker, gc);
        gc.gridy = 1;
        panel.add(title, gc);
        gc.gridy = 2;
        panel.add(copy, gc);
        return panel;
    }

    private JPanel formPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UiTheme.PAGE);

        formCards.setOpaque(false);
        formCards.add(loginCard(), "login");
        formCards.add(registerCard(), "register");

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        wrap.add(formCards, gc);
        return wrap;
    }

    private JPanel loginCard() {
        JTextField username = new JTextField(18);
        JPasswordField password = new JPasswordField(18);
        UiTheme.styleField(username);
        UiTheme.styleField(password);

        JLabel message = errorLabel();
        JButton login = UiTheme.primaryButton("Sign in");
        login.setPreferredSize(new Dimension(320, 48));

        Runnable attempt = () -> {
            try {
                User user = gateway.login(username.getText().trim(), new String(password.getPassword()));
                if (user == null) {
                    message.setText("Invalid username or password");
                    password.setText("");
                    return;
                }
                dispose();
                new MainFrame(gateway, user).setVisible(true);
            } catch (RuntimeException ex) {
                message.setText(ex.getMessage());
            }
        };
        login.addActionListener(e -> attempt.run());
        getRootPane().setDefaultButton(login);

        JButton switcher = link("New here? Create an account");
        switcher.addActionListener(e -> cards.show(formCards, "register"));

        return stack("Welcome back", "Sign in to book tickets or manage shows.",
                fieldBlock("Username", username),
                fieldBlock("Password", password),
                login, message, switcher);
    }

    private JPanel registerCard() {
        JTextField name = new JTextField(18);
        JTextField username = new JTextField(18);
        JPasswordField password = new JPasswordField(18);
        JComboBox<String> role = new JComboBox<>(new String[]{"Clerk", "Admin"});
        UiTheme.styleField(name);
        UiTheme.styleField(username);
        UiTheme.styleField(password);
        role.setBackground(UiTheme.CARD_RAISED);
        role.setForeground(UiTheme.TEXT);

        JLabel message = errorLabel();
        JButton create = UiTheme.primaryButton("Create account");
        create.setPreferredSize(new Dimension(320, 48));
        create.addActionListener(e -> {
            try {
                User user = gateway.register(name.getText().trim(), username.getText().trim(),
                        new String(password.getPassword()), (String) role.getSelectedItem());
                dispose();
                new MainFrame(gateway, user).setVisible(true);
            } catch (RuntimeException ex) {
                message.setText(ex.getMessage());
            }
        });

        JButton switcher = link("Already have an account? Sign in");
        switcher.addActionListener(e -> cards.show(formCards, "login"));

        return stack("Create account", "Clerks book tickets. Admins manage movies and theatres.",
                fieldBlock("Full name", name),
                fieldBlock("Username", username),
                fieldBlock("Password", password),
                fieldBlock("Role", role),
                create, message, switcher);
    }

    private JPanel stack(String title, String subtitle, java.awt.Component... parts) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(360, 560));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 0, 8, 0);
        gc.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel(title);
        heading.setFont(UiTheme.font(Font.BOLD, 26));
        heading.setForeground(UiTheme.TEXT);
        JLabel sub = new JLabel("<html>" + subtitle + "</html>");
        sub.setForeground(UiTheme.MUTED);
        sub.setFont(UiTheme.font(Font.PLAIN, 13));

        int y = 0;
        gc.gridy = y++;
        panel.add(heading, gc);
        gc.gridy = y++;
        gc.insets = new Insets(0, 0, 20, 0);
        panel.add(sub, gc);
        for (java.awt.Component part : parts) {
            gc.gridy = y++;
            gc.insets = new Insets(9, 0, 9, 0);
            panel.add(part, gc);
        }
        return panel;
    }

    private static JPanel fieldBlock(String label, java.awt.Component field) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        JLabel caption = new JLabel(label);
        caption.setForeground(UiTheme.MUTED);
        caption.setFont(UiTheme.font(Font.BOLD, 11));
        panel.add(caption, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel errorLabel() {
        JLabel message = new JLabel(" ", SwingConstants.LEFT);
        message.setForeground(UiTheme.PINK);
        message.setFont(UiTheme.font(Font.PLAIN, 12));
        return message;
    }

    private static JButton link(String text) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(UiTheme.PINK);
        button.setFont(UiTheme.font(Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        return button;
    }
}
