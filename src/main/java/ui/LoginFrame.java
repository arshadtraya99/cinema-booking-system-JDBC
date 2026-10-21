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
    private final AnimatedCardPanel formCards = new AnimatedCardPanel(new CardLayout());
    private JButton loginActionButton;
    private JButton registerActionButton;

    public LoginFrame(CinemaGateway gateway) {
        super("Book Your Show");
        this.gateway = gateway;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 740);
        setMinimumSize(new Dimension(980, 660));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = UiTheme.gradientPanel(new BorderLayout(14, 0));
        UiTheme.pad(root, 14, 14, 14, 14);
        root.add(brandPanel(), BorderLayout.WEST);
        root.add(formPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel brandPanel() {
        JPanel panel = UiTheme.gradientPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(440, 0));

        JPanel identity = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 0));
        identity.setOpaque(false);
        identity.add(new BrandLogo(44));
        JPanel wordmark = new JPanel();
        wordmark.setOpaque(false);
        wordmark.setLayout(new javax.swing.BoxLayout(wordmark, javax.swing.BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("BOOK YOUR SHOW");
        brand.setForeground(Color.WHITE);
        brand.setFont(UiTheme.font(Font.BOLD, 17));
        JLabel subBrand = new JLabel("THE CINEMA DESK");
        subBrand.setForeground(UiTheme.MUTED);
        subBrand.setFont(UiTheme.font(Font.BOLD, 9));
        wordmark.add(brand);
        wordmark.add(javax.swing.Box.createVerticalStrut(4));
        wordmark.add(subBrand);
        identity.add(wordmark);

        JLabel kicker = new JLabel("A BETTER WAY TO BOOK");
        kicker.setForeground(UiTheme.MINT);
        kicker.setFont(UiTheme.font(Font.BOLD, 11));
        kicker.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(UiTheme.MINT.getRed(), UiTheme.MINT.getGreen(),
                        UiTheme.MINT.getBlue(), 90), 1, true),
                BorderFactory.createEmptyBorder(8, 11, 8, 11)));

        JLabel title = new JLabel("<html>Lights down.<br>World on.</html>");
        title.setForeground(Color.WHITE);
        title.setFont(UiTheme.font(Font.BOLD, 38));

        JLabel copy = new JLabel("<html>Discover your next favourite, pick the perfect seat, "
                + "and make tonight feel like a premiere.</html>");
        copy.setForeground(UiTheme.MUTED);
        copy.setFont(UiTheme.font(Font.PLAIN, 14));
        copy.setPreferredSize(new Dimension(326, 62));

        JPanel features = new JPanel();
        features.setOpaque(false);
        features.setLayout(new javax.swing.BoxLayout(features, javax.swing.BoxLayout.Y_AXIS));
        features.add(featureRow("◈", "Live seat map", "Choose seats visually, without the guesswork."));
        features.add(javax.swing.Box.createVerticalStrut(15));
        features.add(featureRow("✦", "One smooth workflow", "Movie discovery, booking, and history in one place."));
        features.add(javax.swing.Box.createVerticalStrut(15));
        features.add(featureRow("✓", "Database-backed", "Bookings are saved through JDBC and MySQL."));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(8, 32, 8, 32);
        panel.add(identity, gc);
        gc.gridy = 1;
        gc.insets = new Insets(52, 32, 12, 32);
        panel.add(kicker, gc);
        gc.gridy = 2;
        gc.insets = new Insets(4, 32, 8, 32);
        panel.add(title, gc);
        gc.gridy = 3;
        gc.insets = new Insets(4, 32, 24, 32);
        panel.add(copy, gc);
        gc.gridy = 4;
        gc.insets = new Insets(4, 32, 8, 32);
        panel.add(features, gc);
        gc.gridy = 5;
        gc.weighty = 1;
        gc.anchor = GridBagConstraints.SOUTHWEST;
        gc.insets = new Insets(22, 32, 14, 32);
        JLabel footer = UiTheme.muted("JAVA SWING  ·  JDBC  ·  MYSQL");
        footer.setFont(UiTheme.font(Font.BOLD, 9));
        panel.add(footer, gc);
        return panel;
    }

    private JPanel featureRow(String symbol, String title, String copy) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel icon = new JLabel(symbol, SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(34, 34));
        icon.setForeground(Color.WHITE);
        icon.setFont(UiTheme.font(Font.BOLD, 16));
        icon.setOpaque(true);
        icon.setBackground(new Color(100, 130, 215, 35));
        icon.setBorder(BorderFactory.createLineBorder(new Color(180, 205, 255, 45), 1, true));
        JPanel words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new javax.swing.BoxLayout(words, javax.swing.BoxLayout.Y_AXIS));
        JLabel titleLabel = UiTheme.heading(title, 12);
        JLabel copyLabel = UiTheme.muted(copy);
        copyLabel.setFont(UiTheme.font(Font.PLAIN, 11));
        words.add(titleLabel);
        words.add(javax.swing.Box.createVerticalStrut(4));
        words.add(copyLabel);
        row.add(icon, BorderLayout.WEST);
        row.add(words, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(350, 48));
        return row;
    }

    private JPanel formPanel() {
        JPanel wrap = UiTheme.gradientPanel(new GridBagLayout());

        formCards.setOpaque(false);
        formCards.add(loginCard(), "login");
        formCards.add(registerCard(), "register");

        JPanel glass = UiTheme.glassPanel(new GridBagLayout());
        glass.setPreferredSize(new Dimension(444, 634));
        glass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 235, 255, 52), 1, true),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)));
        GridBagConstraints inner = new GridBagConstraints();
        inner.gridx = 0;
        inner.gridy = 0;
        inner.weightx = 1;
        inner.fill = GridBagConstraints.HORIZONTAL;
        inner.anchor = GridBagConstraints.CENTER;
        glass.add(formCards, inner);

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        outer.weightx = 1;
        outer.weighty = 1;
        outer.insets = new Insets(8, 8, 8, 8);
        outer.anchor = GridBagConstraints.CENTER;
        wrap.add(glass, outer);
        return wrap;
    }

    private JPanel loginCard() {
        JTextField username = new JTextField(18);
        JPasswordField password = new JPasswordField(18);
        UiTheme.styleField(username);
        UiTheme.styleField(password);

        JLabel message = errorLabel();
        JButton login = UiTheme.primaryButton("Sign in");
        loginActionButton = login;
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
        switcher.addActionListener(e -> {
            formCards.showCard("register");
            if (registerActionButton != null) getRootPane().setDefaultButton(registerActionButton);
        });

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
        registerActionButton = create;
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
        switcher.addActionListener(e -> {
            formCards.showCard("login");
            if (loginActionButton != null) getRootPane().setDefaultButton(loginActionButton);
        });

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
