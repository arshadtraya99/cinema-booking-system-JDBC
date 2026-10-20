package ui;

import ui.CinemaGateway.Booking;
import ui.CinemaGateway.Movie;
import ui.CinemaGateway.Seat;
import ui.CinemaGateway.Show;
import ui.CinemaGateway.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** BookMyShow-style flow: pick a movie, pick a showtime, pick seats. */
public class BookingPanel extends JPanel {

    private static final DateTimeFormatter DATE_LONG = DateTimeFormatter.ofPattern("EEE, dd MMM");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final CinemaGateway gateway;
    private final User clerk;
    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private final JPanel movieGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 18, 18));
    private final JPanel showList = new JPanel();
    private final JPanel dateStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JLabel movieHeading = UiTheme.heading("", 22);
    private final JLabel movieMeta = UiTheme.muted("");
    private final JLabel seatHeading = UiTheme.heading("", 18);
    private final JLabel seatMeta = UiTheme.muted("");
    private final JTextField search = new JTextField(22);
    private final JTextField customerField = new JTextField(16);
    private final JLabel totalLabel = UiTheme.heading("Rs 0", 20);
    private final JLabel selectedLabel = UiTheme.muted("No seats selected");
    private final JButton payButton = UiTheme.primaryButton("Pay");
    private final SeatGridPanel grid;

    private Movie selectedMovie;
    private LocalDate selectedDate;
    private Show selectedShow;
    private final List<JButton> dateButtons = new ArrayList<>();

    public BookingPanel(CinemaGateway gateway, User clerk) {
        this.gateway = gateway;
        this.clerk = clerk;
        this.grid = new SeatGridPanel(this::updateSummary);
        setLayout(new BorderLayout());
        setBackground(UiTheme.PAGE);

        pages.setOpaque(false);
        pages.add(moviesPage(), "movies");
        pages.add(showsPage(), "shows");
        pages.add(seatsPage(), "seats");
        add(pages, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        rebuildMovieGrid(search.getText());
        cards.show(pages, "movies");
    }

    private JPanel moviesPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(UiTheme.PAGE);
        UiTheme.pad(page, 16, 20, 16, 20);

        JLabel title = UiTheme.heading("Now showing", 26);
        JLabel sub = UiTheme.muted("Pick a title, then a theatre and time — just like BookMyShow.");
        UiTheme.styleField(search);
        search.putClientProperty("JTextField.placeholderText", "Search movies");
        search.addActionListener(e -> rebuildMovieGrid(search.getText()));
        JButton go = UiTheme.darkButton("Search");
        go.addActionListener(e -> rebuildMovieGrid(search.getText()));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(sub);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRow.setOpaque(false);
        search.setPreferredSize(new Dimension(240, 36));
        searchRow.add(search);
        searchRow.add(go);
        top.add(titles, BorderLayout.WEST);
        top.add(searchRow, BorderLayout.EAST);

        movieGrid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(movieGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        page.add(top, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel showsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setBackground(UiTheme.PAGE);
        UiTheme.pad(page, 16, 20, 16, 20);

        JButton back = UiTheme.ghostButton("← Movies");
        back.addActionListener(e -> cards.show(pages, "movies"));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(movieHeading);
        titles.add(Box.createVerticalStrut(4));
        titles.add(movieMeta);
        header.add(back, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel datesWrap = new JPanel(new BorderLayout());
        datesWrap.setOpaque(true);
        datesWrap.setBackground(Color.WHITE);
        datesWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.LINE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        dateStrip.setOpaque(false);
        datesWrap.add(dateStrip, BorderLayout.WEST);

        showList.setLayout(new BoxLayout(showList, BoxLayout.Y_AXIS));
        showList.setOpaque(false);
        JScrollPane scroll = new JScrollPane(showList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        page.add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(datesWrap, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel seatsPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Color.WHITE);

        JButton back = UiTheme.ghostButton("← Showtimes");
        back.addActionListener(e -> cards.show(pages, "shows"));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0xFAFAFA));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.LINE));
        UiTheme.pad(header, 10, 16, 10, 16);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(seatHeading);
        titles.add(seatMeta);
        header.add(back, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        UiTheme.styleField(customerField);
        customerField.setPreferredSize(new Dimension(220, 36));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(UiTheme.muted("Customer"));
        left.add(customerField);
        left.add(selectedLabel);

        payButton.setPreferredSize(new Dimension(160, 42));
        payButton.setEnabled(false);
        payButton.addActionListener(e -> book());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.add(totalLabel);
        right.add(payButton);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);

        page.add(header, BorderLayout.NORTH);
        page.add(grid, BorderLayout.CENTER);
        page.add(footer, BorderLayout.SOUTH);
        return page;
    }

    private void rebuildMovieGrid(String query) {
        movieGrid.removeAll();
        String needle = query == null ? "" : query.trim().toLowerCase();
        List<Movie> list = gateway.movies();
        int shown = 0;
        for (Movie movie : list) {
            if (!needle.isEmpty()
                    && !movie.title().toLowerCase().contains(needle)
                    && !movie.genre().toLowerCase().contains(needle)) {
                continue;
            }
            movieGrid.add(movieCard(movie));
            shown++;
        }
        if (shown == 0) {
            movieGrid.add(UiTheme.muted(list.isEmpty()
                    ? "No movies yet. An admin can add titles from the Manage tab."
                    : "No titles match that search."));
        }
        movieGrid.revalidate();
        movieGrid.repaint();
    }

    private JPanel movieCard(Movie movie) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(180, 300));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(UiTheme.LINE));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel poster = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.posterColor(movie.title()));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(-20, -20, 120, 120);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        poster.setOpaque(false);
        poster.setPreferredSize(new Dimension(180, 210));
        JLabel initials = new JLabel(initials(movie.title()), SwingConstants.CENTER);
        initials.setForeground(Color.WHITE);
        initials.setFont(UiTheme.font(Font.BOLD, 28));
        JLabel genre = new JLabel("  " + movie.genre() + "  ", SwingConstants.CENTER);
        genre.setOpaque(true);
        genre.setBackground(UiTheme.PINK);
        genre.setForeground(Color.WHITE);
        genre.setFont(UiTheme.font(Font.BOLD, 11));
        poster.add(initials, BorderLayout.CENTER);
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        badge.setOpaque(false);
        badge.add(genre);
        poster.add(badge, BorderLayout.SOUTH);

        JPanel info = new JPanel();
        info.setOpaque(true);
        info.setBackground(Color.WHITE);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        UiTheme.pad(info, 10, 10, 10, 10);
        JLabel name = new JLabel("<html><b>" + movie.title() + "</b></html>");
        name.setForeground(UiTheme.TEXT);
        JLabel dur = UiTheme.muted(movie.duration() + " min  ·  Book tickets");
        info.add(name);
        info.add(Box.createVerticalStrut(4));
        info.add(dur);

        card.add(poster, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { openMovie(movie); }
        });
        return card;
    }

    private void openMovie(Movie movie) {
        selectedMovie = movie;
        movieHeading.setText(movie.title());
        movieMeta.setText(movie.genre() + "  ·  " + movie.duration() + " min");
        List<Show> shows = gateway.showsForMovie(movie.id()).stream()
                .sorted(Comparator.comparing(Show::date).thenComparing(Show::time))
                .toList();
        List<LocalDate> dates = shows.stream().map(Show::date).distinct().toList();
        selectedDate = dates.isEmpty() ? null : dates.get(0);
        rebuildDateStrip(dates);
        rebuildShowList(shows);
        cards.show(pages, "shows");
    }

    private void rebuildDateStrip(List<LocalDate> dates) {
        dateStrip.removeAll();
        dateButtons.clear();
        if (dates.isEmpty()) {
            dateStrip.add(UiTheme.muted("No showtimes yet for this title."));
        }
        for (LocalDate date : dates) {
            JButton chip = UiTheme.ghostButton(date.format(DateTimeFormatter.ofPattern("EEE dd MMM")));
            if (date.equals(selectedDate)) styleDateChip(chip, true);
            chip.addActionListener(e -> {
                selectedDate = date;
                for (JButton button : dateButtons) styleDateChip(button, false);
                styleDateChip(chip, true);
                rebuildShowList(gateway.showsForMovie(selectedMovie.id()));
            });
            dateButtons.add(chip);
            dateStrip.add(chip);
        }
        dateStrip.revalidate();
        dateStrip.repaint();
    }

    private void styleDateChip(JButton button, boolean selected) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(selected ? UiTheme.PINK : Color.WHITE);
        button.setForeground(selected ? Color.WHITE : UiTheme.TEXT);
    }

    private void rebuildShowList(List<Show> all) {
        showList.removeAll();
        List<Show> filtered = all.stream()
                .filter(show -> selectedDate == null || show.date().equals(selectedDate))
                .sorted(Comparator.comparing(Show::theatreName).thenComparing(Show::time))
                .toList();
        Map<String, List<Show>> byTheatre = new LinkedHashMap<>();
        for (Show show : filtered) {
            byTheatre.computeIfAbsent(show.theatreName(), key -> new ArrayList<>()).add(show);
        }
        if (byTheatre.isEmpty()) {
            showList.add(UiTheme.muted("No shows on this date."));
        }
        for (Map.Entry<String, List<Show>> entry : byTheatre.entrySet()) {
            showList.add(theatreCard(entry.getKey(), entry.getValue()));
            showList.add(Box.createVerticalStrut(12));
        }
        showList.revalidate();
        showList.repaint();
    }

    private JPanel theatreCard(String theatreName, List<Show> shows) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.LINE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        Show first = shows.get(0);
        JLabel name = UiTheme.heading(theatreName, 16);
        JLabel loc = UiTheme.muted(first.screenName() + " and more screens");
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(name);
        titles.add(loc);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        chips.setOpaque(false);
        for (Show show : shows) {
            chips.add(showChip(show));
        }
        card.add(titles, BorderLayout.NORTH);
        card.add(chips, BorderLayout.CENTER);
        return card;
    }

    private JButton showChip(Show show) {
        JButton chip = new JButton(show.time().format(TIME));
        chip.setFocusPainted(false);
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.setBackground(Color.WHITE);
        chip.setForeground(UiTheme.SHOW_GREEN);
        chip.setFont(UiTheme.font(Font.BOLD, 13));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.SHOW_GREEN),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        chip.setToolTipText(show.screenName());
        chip.addActionListener(e -> openSeats(show));
        return chip;
    }

    private void openSeats(Show show) {
        selectedShow = show;
        seatHeading.setText(show.movieTitle());
        seatMeta.setText(show.screenLabel() + "   ·   " + show.date().format(DATE_LONG)
                + "   ·   " + show.time().format(TIME));
        grid.setSeats(gateway.seats(show.id()));
        customerField.setText("");
        cards.show(pages, "seats");
    }

    private void updateSummary(List<Seat> selected) {
        if (selected.isEmpty()) {
            selectedLabel.setText("No seats selected");
            totalLabel.setText("Rs 0");
            payButton.setEnabled(false);
            return;
        }
        selectedLabel.setText(selected.stream().map(Seat::label).collect(Collectors.joining(", ")));
        BigDecimal total = selected.stream().map(Seat::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Rs " + total);
        payButton.setEnabled(true);
    }

    private void book() {
        if (selectedShow == null) return;
        List<Seat> selected = grid.getSelectedSeats();
        String customer = customerField.getText().trim();
        if (selected.isEmpty()) return;
        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the customer name to continue.",
                    "Customer required", JOptionPane.WARNING_MESSAGE);
            customerField.requestFocus();
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Pay " + totalLabel.getText() + " for " + selected.size() + " seat(s)?",
                "Confirm payment", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            List<Integer> ids = selected.stream().map(Seat::id).toList();
            List<Booking> done = gateway.bookSeats(selectedShow.id(), ids, clerk.id(), customer);
            showReceipt(selectedShow, customer, done);
            customerField.setText("");
            grid.setSeats(gateway.seats(selectedShow.id()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking failed", JOptionPane.ERROR_MESSAGE);
            grid.setSeats(gateway.seats(selectedShow.id()));
        }
    }

    private void showReceipt(Show show, String customer, List<Booking> done) {
        BigDecimal total = done.stream().map(Booking::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        StringBuilder sb = new StringBuilder();
        sb.append("===========  CINEBOOK TICKET  ===========\n");
        sb.append(String.format("Movie    : %s%n", show.movieTitle()));
        sb.append(String.format("Venue    : %s%n", show.screenLabel()));
        sb.append(String.format("Date     : %s%n", show.date().format(DATE_LONG)));
        sb.append(String.format("Time     : %s%n", show.time().format(TIME)));
        sb.append(String.format("Customer : %s%n", customer));
        sb.append(String.format("Issued by: %s%n", clerk.name()));
        sb.append("-----------------------------------------\n");
        for (Booking booking : done) {
            sb.append(String.format("Seat %-8s              Rs %s%n", booking.seatLabel(), booking.amount()));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("AMOUNT PAID                     Rs %s%n", total));
        sb.append("=========================================\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, area, "Booking confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String initials(String title) {
        if (title == null || title.isBlank()) return "CB";
        String[] parts = title.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }
}
