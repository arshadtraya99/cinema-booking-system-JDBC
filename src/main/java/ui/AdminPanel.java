package ui;

import ui.CinemaGateway.Movie;
import ui.CinemaGateway.Screen;
import ui.CinemaGateway.SeatCategory;
import ui.CinemaGateway.Show;
import ui.CinemaGateway.Theatre;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AdminPanel extends JTabbedPane {

    private final CinemaGateway gateway;
    private final DefaultTableModel movieModel = readOnly("ID", "Title", "Duration (min)", "Genre");
    private final DefaultTableModel showModel = readOnly("ID", "Movie", "Theatre / screen", "Date", "Time");
    private final DefaultTableModel theatreModel = readOnly("ID", "Theatre", "Location", "City");
    private final DefaultTableModel screenModel = readOnly("ID", "Theatre", "Screen", "Seats");
    private final DefaultTableModel priceModel = readOnly("ID", "Category", "Price (Rs)");
    private final JComboBox<Movie> showMovieBox = new JComboBox<>();
    private final JComboBox<Screen> screenBox = new JComboBox<>();
    private final JComboBox<Theatre> theatreBox = new JComboBox<>();

    public AdminPanel(CinemaGateway gateway) {
        this.gateway = gateway;
        setBackground(UiTheme.PAGE);
        addTab("Movies", buildMovies());
        addTab("Showtimes", buildShows());
        addTab("Theatres", buildTheatres());
        addTab("Screens", buildScreens());
        addTab("Pricing", buildPricing());
        refresh();
    }

    private static DefaultTableModel readOnly(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JPanel buildMovies() {
        JTable table = table(movieModel);
        JTextField title = field(14);
        JTextField duration = field(5);
        JTextField genre = field(10);
        JButton add = UiTheme.primaryButton("Add movie");
        JButton del = UiTheme.ghostButton("Delete selected");

        add.addActionListener(e -> {
            try {
                String value = title.getText().trim();
                if (value.isEmpty()) throw new IllegalArgumentException("Title is required.");
                int minutes = Integer.parseInt(duration.getText().trim());
                if (minutes <= 0) throw new NumberFormatException();
                gateway.addMovie(value, minutes, genre.getText().trim());
                title.setText("");
                duration.setText("");
                genre.setText("");
                refresh();
            } catch (NumberFormatException ex) {
                error("Duration must be a positive whole number of minutes.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        del.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { error("Select a movie first."); return; }
            int ok = JOptionPane.showConfirmDialog(this,
                    "Delete this movie and all of its shows?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            try {
                gateway.deleteMovie((Integer) movieModel.getValueAt(row, 0));
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        return page("Movies", "Add new releases and retire old titles.", table,
                row("Title", title, "Duration", duration, "Genre", genre, add, del));
    }

    private JPanel buildShows() {
        JTable table = table(showModel);
        JTextField date = field(10);
        date.setText(LocalDate.now().toString());
        JTextField time = field(6);
        time.setText("18:30");
        JButton add = UiTheme.primaryButton("Schedule show");
        add.addActionListener(e -> {
            Movie movie = (Movie) showMovieBox.getSelectedItem();
            Screen screen = (Screen) screenBox.getSelectedItem();
            if (movie == null || screen == null) {
                error("Add a movie, theatre, and screen first.");
                return;
            }
            try {
                gateway.addShow(movie.id(), screen.id(),
                        LocalDate.parse(date.getText().trim()),
                        LocalTime.parse(time.getText().trim()));
                refresh();
            } catch (DateTimeParseException ex) {
                error("Use date format yyyy-MM-dd and time format HH:mm.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        return page("Showtimes", "Scheduling a show also lays out the seat map for that screen.", table,
                row("Movie", showMovieBox, "Screen", screenBox, "Date", date, "Time", time, add));
    }

    private JPanel buildTheatres() {
        JTable table = table(theatreModel);
        JTextField name = field(12);
        JTextField location = field(12);
        JTextField city = field(10);
        JButton add = UiTheme.primaryButton("Add theatre");
        JButton del = UiTheme.ghostButton("Delete selected");
        add.addActionListener(e -> {
            try {
                if (name.getText().trim().isEmpty() || city.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Theatre name and city are required.");
                }
                gateway.addTheatre(name.getText().trim(), location.getText().trim(), city.getText().trim());
                name.setText("");
                location.setText("");
                city.setText("");
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        del.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { error("Select a theatre first."); return; }
            try {
                gateway.deleteTheatre((Integer) theatreModel.getValueAt(row, 0));
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        return page("Theatres", "Locations that appear on the showtime listing.", table,
                row("Name", name, "Location", location, "City", city, add, del));
    }

    private JPanel buildScreens() {
        JTable table = table(screenModel);
        JTextField name = field(10);
        JTextField seats = field(5);
        seats.setText("80");
        JButton add = UiTheme.primaryButton("Add screen");
        JButton del = UiTheme.ghostButton("Delete selected");
        add.addActionListener(e -> {
            Theatre theatre = (Theatre) theatreBox.getSelectedItem();
            if (theatre == null) { error("Add a theatre first."); return; }
            try {
                int total = Integer.parseInt(seats.getText().trim());
                if (total <= 0) throw new NumberFormatException();
                gateway.addScreen(theatre.id(), name.getText().trim(), total);
                name.setText("");
                refresh();
            } catch (NumberFormatException ex) {
                error("Total seats must be a positive whole number.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        del.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { error("Select a screen first."); return; }
            try {
                gateway.deleteScreen((Integer) screenModel.getValueAt(row, 0));
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        return page("Screens", "Each screen gets its own seat map when a show is scheduled.", table,
                row("Theatre", theatreBox, "Screen name", name, "Total seats", seats, add, del));
    }

    private JPanel buildPricing() {
        JTable table = table(priceModel);
        JTextField name = field(10);
        JTextField price = field(6);
        JButton add = UiTheme.primaryButton("Add category");
        add.addActionListener(e -> {
            try {
                if (name.getText().trim().isEmpty()) throw new IllegalArgumentException("Category name is required.");
                gateway.addCategory(name.getText().trim(), new BigDecimal(price.getText().trim()));
                name.setText("");
                price.setText("");
                refresh();
            } catch (NumberFormatException ex) {
                error("Price must be a number, for example 200 or 199.00.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        return page("Seat pricing", "Platinum, Gold, and Silver tiers used on the seat map.", table,
                row("Category", name, "Price", price, add));
    }

    public void refresh() {
        movieModel.setRowCount(0);
        showMovieBox.removeAllItems();
        for (Movie movie : gateway.movies()) {
            movieModel.addRow(new Object[]{movie.id(), movie.title(), movie.duration(), movie.genre()});
            showMovieBox.addItem(movie);
        }

        theatreModel.setRowCount(0);
        theatreBox.removeAllItems();
        for (Theatre theatre : gateway.theatres()) {
            theatreModel.addRow(new Object[]{theatre.id(), theatre.name(), theatre.location(), theatre.city()});
            theatreBox.addItem(theatre);
        }

        screenModel.setRowCount(0);
        screenBox.removeAllItems();
        for (Screen screen : gateway.screens()) {
            screenModel.addRow(new Object[]{screen.id(), screen.theatreName(), screen.name(), screen.totalSeats()});
            screenBox.addItem(screen);
        }

        priceModel.setRowCount(0);
        for (SeatCategory category : gateway.categories()) {
            priceModel.addRow(new Object[]{category.id(), category.name(), category.price()});
        }

        showModel.setRowCount(0);
        DateTimeFormatter dates = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter times = DateTimeFormatter.ofPattern("HH:mm");
        for (Show show : gateway.shows()) {
            showModel.addRow(new Object[]{show.id(), show.movieTitle(), show.screenLabel(),
                    show.date().format(dates), show.time().format(times)});
        }
    }

    private JPanel page(String title, String subtitle, JTable table, JPanel form) {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setBackground(UiTheme.PAGE);
        UiTheme.pad(page, 16, 16, 12, 16);
        JLabel heading = UiTheme.heading(title, 20);
        JLabel sub = UiTheme.muted(subtitle);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new javax.swing.BoxLayout(titles, javax.swing.BoxLayout.Y_AXIS));
        titles.add(heading);
        titles.add(sub);
        top.add(titles, BorderLayout.WEST);
        page.add(top, BorderLayout.NORTH);
        page.add(UiTheme.scroll(table), BorderLayout.CENTER);
        form.setOpaque(true);
        form.setBackground(Color.WHITE);
        form.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UiTheme.LINE),
                javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        page.add(form, BorderLayout.SOUTH);
        return page;
    }

    private static JPanel row(Object... parts) {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.setOpaque(false);
        for (Object part : parts) {
            if (part instanceof String text) form.add(new JLabel(text));
            else form.add((java.awt.Component) part);
        }
        return form;
    }

    private static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        UiTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        UiTheme.styleField(field);
        return field;
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Could not save", JOptionPane.ERROR_MESSAGE);
    }
}
