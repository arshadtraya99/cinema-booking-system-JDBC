package ui;

import ui.CinemaGateway.Booking;
import ui.CinemaGateway.Movie;
import ui.CinemaGateway.Screen;
import ui.CinemaGateway.Seat;
import ui.CinemaGateway.Show;
import ui.CinemaGateway.Theatre;
import ui.CinemaGateway.User;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** BookMyShow-style flow: pick a movie, pick a showtime, pick seats. */
public class BookingPanel extends JPanel {

    private static final DateTimeFormatter DATE_LONG = DateTimeFormatter.ofPattern("EEE, dd MMM");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final CinemaGateway gateway;
    private final User clerk;
    private final AnimatedCardPanel pages = new AnimatedCardPanel(new CardLayout());
    private static final Pattern YOUTUBE_ID = Pattern.compile(
            "(?:youtu\\.be/|youtube\\.com/(?:watch\\?v=|embed/|shorts/|live/))([A-Za-z0-9_-]{11})");

    private final JPanel movieGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 18));
    private final JPanel showList = new JPanel();
    private final JPanel dateStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JPanel trailerSection = new JPanel(new BorderLayout());
    private final JLabel movieHeading = UiTheme.heading("", 22);
    private final JLabel movieMeta = UiTheme.muted("");
    private final JLabel seatHeading = UiTheme.heading("", 18);
    private final JLabel seatMeta = UiTheme.muted("");
    private final JTextField search = new JTextField(22) {
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(getFont());
                g.setColor(new Color(UiTheme.MUTED.getRed(), UiTheme.MUTED.getGreen(), UiTheme.MUTED.getBlue(), 175));
                int baseline = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
                g.drawString("Search movies...", getInsets().left + 2, baseline);
                g.dispose();
            }
        }
    };
    private final JComboBox<LocationFilter> locationFilter = new JComboBox<>();
    private final JComboBox<String> genreFilter = new JComboBox<>();
    private final JLabel resultsLabel = UiTheme.muted("");
    private final JTextField customerField = new JTextField(16);
    private final JLabel totalLabel = UiTheme.heading("Rs 0", 20);
    private final JLabel selectedLabel = UiTheme.muted("No seats selected");
    private final JButton payButton = UiTheme.primaryButton("Pay");
    private final SeatGridPanel grid;

    private Movie selectedMovie;
    private LocalDate selectedDate;
    private Show selectedShow;
    private boolean updatingGenres;
    private boolean updatingLocations;
    private List<Movie> movieCatalog = List.of();
    private List<Theatre> theatreCatalog = List.of();
    private List<Screen> screenCatalog = List.of();
    private List<Show> showCatalog = List.of();
    private final List<JButton> dateButtons = new ArrayList<>();

    private record LocationFilter(String location, String city) {
        private LocationFilter {
            location = location == null ? "" : location.trim();
            city = city == null ? "" : city.trim();
        }

        private boolean isAll() {
            return location.isEmpty() && city.isEmpty();
        }

        @Override public String toString() {
            if (isAll()) return "All locations";
            if (location.isEmpty()) return city;
            if (city.isEmpty()) return location;
            return location + "  ·  " + city;
        }
    }

    public BookingPanel(CinemaGateway gateway, User clerk) {
        this.gateway = gateway;
        this.clerk = clerk;
        this.grid = new SeatGridPanel(this::updateSummary);
        setLayout(new BorderLayout());
        setOpaque(false);

        pages.setOpaque(false);
        pages.add(moviesPage(), "movies");
        pages.add(showsPage(), "shows");
        pages.add(seatsPage(), "seats");
        add(pages, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        LocationFilter selectedLocation = (LocationFilter) locationFilter.getSelectedItem();
        movieCatalog = gateway.movies();
        theatreCatalog = gateway.theatres();
        screenCatalog = gateway.screens();
        showCatalog = gateway.shows();

        updatingLocations = true;
        locationFilter.removeAllItems();
        LocationFilter allLocations = new LocationFilter("", "");
        locationFilter.addItem(allLocations);
        theatreCatalog.stream()
                .map(theatre -> new LocationFilter(theatre.location(), theatre.city()))
                .filter(location -> !location.isAll())
                .distinct()
                .sorted(Comparator.comparing(LocationFilter::toString, String.CASE_INSENSITIVE_ORDER))
                .forEach(locationFilter::addItem);
        if (selectedLocation != null && containsLocation(selectedLocation)) {
            locationFilter.setSelectedItem(selectedLocation);
        } else {
            locationFilter.setSelectedItem(allLocations);
        }
        updatingLocations = false;

        String selectedGenre = (String) genreFilter.getSelectedItem();
        updatingGenres = true;
        genreFilter.removeAllItems();
        genreFilter.addItem("All genres");
        TreeSet<String> genres = new TreeSet<>(movieCatalog.stream()
                .map(Movie::genre)
                .filter(genre -> genre != null && !genre.isBlank())
                .toList());
        genres.forEach(genreFilter::addItem);
        if (selectedGenre != null && genres.contains(selectedGenre)) genreFilter.setSelectedItem(selectedGenre);
        else genreFilter.setSelectedItem("All genres");
        updatingGenres = false;
        rebuildMovieGrid(search.getText());
        pages.showCard("movies");
    }

    private JPanel moviesPage() {
        JPanel page = UiTheme.gradientPanel(new BorderLayout(0, 14));
        UiTheme.pad(page, 26, 28, 24, 28);

        JLabel title = UiTheme.heading("Find your next favourite", 28);
        JLabel sub = UiTheme.muted("Choose a film, find your showtime, and settle into the perfect seat.");
        UiTheme.styleField(search);
        search.putClientProperty("JTextField.placeholderText", "Search movies");
        Timer searchDebounce = new Timer(180, e -> rebuildMovieGrid(search.getText()));
        searchDebounce.setRepeats(false);
        search.addActionListener(e -> {
            searchDebounce.stop();
            rebuildMovieGrid(search.getText());
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDebounce.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDebounce.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDebounce.restart(); }
        });
        genreFilter.setBackground(UiTheme.CARD_RAISED);
        genreFilter.setForeground(UiTheme.TEXT);
        genreFilter.setFont(UiTheme.font(Font.PLAIN, 13));
        genreFilter.setPreferredSize(new Dimension(150, 42));
        UiTheme.centerComboBox(genreFilter);
        genreFilter.addActionListener(e -> {
            if (!updatingGenres) {
                searchDebounce.stop();
                rebuildMovieGrid(search.getText());
            }
        });
        locationFilter.setBackground(UiTheme.CARD_RAISED);
        locationFilter.setForeground(UiTheme.TEXT);
        locationFilter.setFont(UiTheme.font(Font.PLAIN, 13));
        locationFilter.setPreferredSize(new Dimension(170, 42));
        UiTheme.centerComboBox(locationFilter);
        locationFilter.addActionListener(e -> {
            if (!updatingLocations) {
                searchDebounce.stop();
                rebuildMovieGrid(search.getText());
            }
        });
        JButton clear = UiTheme.ghostButton("Clear");
        clear.setToolTipText("Clear movie search");
        clear.addActionListener(e -> {
            searchDebounce.stop();
            search.setText("");
            genreFilter.setSelectedItem("All genres");
            locationFilter.setSelectedIndex(0);
            rebuildMovieGrid(search.getText());
            search.requestFocusInWindow();
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(6));
        titles.add(sub);
        titles.add(Box.createVerticalStrut(5));
        titles.add(resultsLabel);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchRow.setOpaque(false);
        search.setPreferredSize(new Dimension(190, 42));
        searchRow.add(UiTheme.muted("Location"));
        searchRow.add(locationFilter);
        searchRow.add(genreFilter);
        searchRow.add(search);
        searchRow.add(clear);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        top.add(titles, BorderLayout.NORTH);
        top.add(searchRow, BorderLayout.SOUTH);

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
        JPanel page = UiTheme.gradientPanel(new BorderLayout(0, 12));
        UiTheme.pad(page, 22, 26, 22, 26);

        JButton back = UiTheme.ghostButton("← Movies");
        back.addActionListener(e -> pages.showCard("movies"));

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(movieHeading);
        titles.add(Box.createVerticalStrut(4));
        titles.add(movieMeta);
        header.add(back, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel datesWrap = UiTheme.glassPanel(new BorderLayout());
        datesWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 28), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        dateStrip.setOpaque(false);
        datesWrap.add(dateStrip, BorderLayout.WEST);

        showList.setLayout(new BoxLayout(showList, BoxLayout.Y_AXIS));
        showList.setOpaque(false);
        JScrollPane scroll = new JScrollPane(showList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        trailerSection.setOpaque(false);

        page.add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(trailerSection, BorderLayout.NORTH);
        top.add(datesWrap, BorderLayout.SOUTH);
        center.add(top, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel seatsPage() {
        JPanel page = UiTheme.gradientPanel(new BorderLayout());

        JButton back = UiTheme.ghostButton("← Showtimes");
        back.addActionListener(e -> pages.showCard("shows"));

        JPanel header = UiTheme.glassPanel(new BorderLayout(18, 0));
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(seatHeading);
        titles.add(seatMeta);
        header.add(back, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel footer = UiTheme.glassPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        UiTheme.styleField(customerField);
        customerField.setPreferredSize(new Dimension(220, 44));
        customerField.setToolTipText("Enter the customer's name");
        customerField.addActionListener(e -> book());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(UiTheme.muted("Customer"));
        left.add(customerField);
        left.add(selectedLabel);

        payButton.setText("Confirm booking");
        payButton.setToolTipText("Confirm payment for the selected seats");
        payButton.setPreferredSize(new Dimension(188, 48));
        payButton.setEnabled(false);
        payButton.addActionListener(e -> book());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
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
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String selectedGenre = (String) genreFilter.getSelectedItem();
        LocationFilter selectedLocation = (LocationFilter) locationFilter.getSelectedItem();
        Set<Integer> moviesAtLocation = selectedLocation == null || selectedLocation.isAll()
                ? Set.of()
                : movieIdsAtLocation(selectedLocation);
        List<Movie> list = movieCatalog.stream()
                .filter(movie -> selectedLocation == null || selectedLocation.isAll()
                        || moviesAtLocation.contains(movie.id()))
                .toList();
        int shown = 0;
        for (Movie movie : list) {
            String genre = movie.genre() == null ? "" : movie.genre();
            if (!needle.isEmpty()
                    && !movie.title().toLowerCase(Locale.ROOT).contains(needle)
                    && !genre.toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            if (selectedGenre != null && !"All genres".equals(selectedGenre)
                    && !selectedGenre.equals(movie.genre())) {
                continue;
            }
            movieGrid.add(movieCard(movie));
            shown++;
        }
        resultsLabel.setText(shown + (shown == 1 ? " film" : " films") + " to explore");
        if (shown == 0) {
            JLabel emptyTitle = UiTheme.heading(list.isEmpty() ? "Your screen is waiting" : "No films found", 18);
            JLabel emptyCopy = UiTheme.muted(list.isEmpty()
                    ? (selectedLocation != null && !selectedLocation.isAll()
                        ? "No movies have showtimes at this location yet."
                        : "An admin can add movies from the Manage section.")
                    : "Try another title, genre, or location, or clear your filters.");
            JPanel empty = UiTheme.glassPanel(new BorderLayout());
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 28), 1, true),
                    BorderFactory.createEmptyBorder(24, 28, 24, 28)));
            empty.add(emptyTitle);
            empty.add(Box.createVerticalStrut(6));
            empty.add(emptyCopy);
            movieGrid.add(empty);
        }
        movieGrid.revalidate();
        movieGrid.repaint();
    }

    private boolean containsLocation(LocationFilter location) {
        return theatreCatalog.stream()
                .map(theatre -> new LocationFilter(theatre.location(), theatre.city()))
                .anyMatch(location::equals);
    }

    private Set<Integer> movieIdsAtLocation(LocationFilter location) {
        Set<Integer> theatreIds = theatreCatalog.stream()
                .filter(theatre -> location.equals(new LocationFilter(theatre.location(), theatre.city())))
                .map(Theatre::id)
                .collect(Collectors.toSet());
        Set<Integer> screenIds = screenCatalog.stream()
                .filter(screen -> theatreIds.contains(screen.theatreId()))
                .map(Screen::id)
                .collect(Collectors.toSet());
        return showCatalog.stream()
                .filter(show -> screenIds.contains(show.screenId()))
                .map(Show::movieId)
                .collect(Collectors.toSet());
    }

    private JPanel movieCard(Movie movie) {
        JPanel card = UiTheme.glassPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(198, 336));
        card.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 28), 1, true));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Image posterImage = decodePoster(movie.poster());
        JPanel poster = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (posterImage != null) {
                    int iw = posterImage.getWidth(null);
                    int ih = posterImage.getHeight(null);
                    if (iw > 0 && ih > 0) {
                        double scale = Math.max(getWidth() / (double) iw, getHeight() / (double) ih);
                        int w = (int) Math.round(iw * scale);
                        int h = (int) Math.round(ih * scale);
                        g2.drawImage(posterImage, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
                    }
                } else {
                    g2.setColor(UiTheme.posterColor(movie.title()));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillOval(-20, -20, 120, 120);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        poster.setOpaque(false);
        poster.setPreferredSize(new Dimension(198, 232));
        JLabel initials = new JLabel(posterImage == null ? initials(movie.title()) : "", SwingConstants.CENTER);
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

        JPanel info = UiTheme.glassPanel(new BorderLayout());
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        UiTheme.pad(info, 14, 14, 14, 14);
        JLabel name = new JLabel("<html><b>" + movie.title() + "</b></html>");
        name.setForeground(UiTheme.TEXT);
        JLabel dur = UiTheme.muted(movie.duration() + " min  ·  Book tickets");
        info.add(name);
        info.add(Box.createVerticalStrut(7));
        info.add(dur);

        card.add(poster, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);
        java.awt.event.MouseAdapter openAction = new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { openMovie(movie); }
        };
        addMovieCardAction(card, openAction);
        card.setFocusable(true);
        card.getInputMap(JPanel.WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "openMovie");
        card.getActionMap().put("openMovie", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { openMovie(movie); }
        });
        card.getAccessibleContext().setAccessibleName(movie.title());
        card.getAccessibleContext().setAccessibleDescription(
                movie.genre() + ", " + movie.duration() + " minutes. Press Enter to view showtimes.");
        return card;
    }

    private static void addMovieCardAction(java.awt.Container parent, java.awt.event.MouseListener listener) {
        if (parent instanceof JPanel root) {
            addMovieCardAction(root, parent, listener);
        } else {
            parent.addMouseListener(listener);
        }
    }

    private static void addMovieCardAction(JPanel root, java.awt.Container current,
                                           java.awt.event.MouseListener listener) {
        current.addMouseListener(listener);
        current.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent event) {
                root.putClientProperty("hoverGlow", Boolean.TRUE);
                root.repaint();
            }

            @Override public void mouseExited(java.awt.event.MouseEvent event) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    boolean inside = root.getMousePosition(true) != null;
                    root.putClientProperty("hoverGlow", inside ? Boolean.TRUE : Boolean.FALSE);
                    root.repaint();
                });
            }
        });
        for (java.awt.Component child : current.getComponents()) {
            if (child instanceof java.awt.Container container) {
                addMovieCardAction(root, container, listener);
            } else {
                child.addMouseListener(listener);
                child.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent event) {
                        root.putClientProperty("hoverGlow", Boolean.TRUE);
                        root.repaint();
                    }

                    @Override public void mouseExited(java.awt.event.MouseEvent event) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            boolean inside = root.getMousePosition(true) != null;
                            root.putClientProperty("hoverGlow", inside ? Boolean.TRUE : Boolean.FALSE);
                            root.repaint();
                        });
                    }
                });
            }
        }
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
        rebuildTrailerSection(movie);
        rebuildDateStrip(dates);
        rebuildShowList(shows);
        pages.showCard("shows");
    }

    private void rebuildTrailerSection(Movie movie) {
        trailerSection.removeAll();
        JPanel card = UiTheme.glassPanel(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 28), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 20)));

        Image posterImage = decodePoster(movie.poster());
        JPanel preview = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (posterImage != null) {
                    int iw = posterImage.getWidth(null);
                    int ih = posterImage.getHeight(null);
                    if (iw > 0 && ih > 0) {
                        double scale = Math.max(getWidth() / (double) iw, getHeight() / (double) ih);
                        int w = (int) Math.round(iw * scale);
                        int h = (int) Math.round(ih * scale);
                        g2.drawImage(posterImage, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
                    }
                } else {
                    g2.setColor(UiTheme.posterColor(movie.title()));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                int size = 46;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillOval(x, y, size, size);
                g2.setColor(UiTheme.PINK);
                int[] px = {x + 17, x + 17, x + 34};
                int[] py = {y + 13, y + 33, y + 23};
                g2.fillPolygon(px, py, 3);
                g2.dispose();
            }
        };
        preview.setPreferredSize(new Dimension(220, 124));
        preview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        preview.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { playTrailer(movie); }
        });

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(UiTheme.heading("Watch trailer", 16));
        copy.add(Box.createVerticalStrut(7));
        copy.add(UiTheme.muted("Play the official trailer before you pick seats."));
        copy.add(Box.createVerticalStrut(16));
        JButton play = UiTheme.primaryButton("▶  Play trailer");
        play.setAlignmentX(LEFT_ALIGNMENT);
        play.addActionListener(e -> playTrailer(movie));
        copy.add(play);

        card.add(preview, BorderLayout.WEST);
        card.add(copy, BorderLayout.CENTER);
        trailerSection.add(card, BorderLayout.CENTER);
        trailerSection.revalidate();
        trailerSection.repaint();
    }

    private void playTrailer(Movie movie) {
        String url = trailerWatchUrl(movie);
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new IllegalStateException("This computer cannot open a browser.");
            }
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not play the trailer.\n" + (ex.getMessage() == null ? url : ex.getMessage()),
                    "Trailer", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String trailerWatchUrl(Movie movie) {
        String stored = movie.trailerUrl() == null ? "" : movie.trailerUrl().trim();
        String videoId = youtubeId(stored);
        if (videoId != null) return "https://www.youtube.com/watch?v=" + videoId + "&autoplay=1";
        if (!stored.isEmpty() && (stored.startsWith("http://") || stored.startsWith("https://"))) return stored;
        String query = URLEncoder.encode(movie.title() + " official trailer", StandardCharsets.UTF_8);
        return "https://www.youtube.com/results?search_query=" + query;
    }

    private static String youtubeId(String url) {
        if (url == null || url.isBlank()) return null;
        Matcher matcher = YOUTUBE_ID.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
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
        button.setBackground(selected ? UiTheme.PINK : UiTheme.CARD_RAISED);
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
        JPanel card = UiTheme.glassPanel(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 28), 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        Show first = shows.get(0);
        JLabel name = UiTheme.heading(theatreName, 16);
        JLabel loc = UiTheme.muted(first.screenName() + "  ·  " + shows.size()
                + (shows.size() == 1 ? " showtime" : " showtimes"));
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
        chip.setBackground(UiTheme.CARD_RAISED);
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
        pages.showCard("seats");
    }

    private void updateSummary(List<Seat> selected) {
        if (selected.isEmpty()) {
            selectedLabel.setText("No seats selected");
            selectedLabel.setToolTipText(null);
            totalLabel.setText("Rs 0");
            payButton.setEnabled(false);
            return;
        }
        String seatNames = selected.stream().map(Seat::label).collect(Collectors.joining(", "));
        String shortList = seatNames.length() > 28 ? seatNames.substring(0, 25) + "..." : seatNames;
        selectedLabel.setText(selected.size() + (selected.size() == 1 ? " seat" : " seats")
                + " selected  ·  " + shortList);
        selectedLabel.setToolTipText(seatNames);
        selectedLabel.getAccessibleContext().setAccessibleDescription(
                selected.size() + " seats selected: " + seatNames);
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
            ToastNotification.show(this, "Booking confirmed",
                    done.size() + (done.size() == 1 ? " ticket is" : " tickets are") + " ready for " + customer + ".");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking failed", JOptionPane.ERROR_MESSAGE);
            grid.setSeats(gateway.seats(selectedShow.id()));
        }
    }

    private void showReceipt(Show show, String customer, List<Booking> done) {
        BigDecimal total = done.stream().map(Booking::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        StringBuilder sb = new StringBuilder();
        sb.append("========  BOOK YOUR SHOW TICKET  ========\n");
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

    private static Image decodePoster(byte[] data) {
        if (data == null || data.length == 0) return null;
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            return image;
        } catch (IOException e) {
            return null;
        }
    }

    private static String initials(String title) {
        if (title == null || title.isBlank()) return "CB";
        String[] parts = title.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }
}
