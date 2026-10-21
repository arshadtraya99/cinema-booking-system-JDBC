package ui;

import ui.CinemaGateway.Movie;
import ui.CinemaGateway.Screen;
import ui.CinemaGateway.SeatCategory;
import ui.CinemaGateway.Show;
import ui.CinemaGateway.Theatre;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
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
        setOpaque(false);
        UiTheme.styleTabbedPane(this);
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
        JTextField trailer = field(18);
        JLabel posterName = UiTheme.muted("No photo");
        byte[][] poster = {null};
        JButton pickPhoto = UiTheme.ghostButton("Choose photo");
        JButton add = UiTheme.primaryButton("Add movie");
        JButton edit = UiTheme.ghostButton("Edit selected");
        JButton cancel = UiTheme.ghostButton("Cancel");
        JButton del = UiTheme.ghostButton("Delete selected");
        cancel.setEnabled(false);
        int[] editingId = {0};

        pickPhoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Movie photo");
            chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File file = chooser.getSelectedFile();
            try {
                if (file.length() > 4L * 1024 * 1024) {
                    throw new IllegalArgumentException("Photo must be 4 MB or smaller.");
                }
                BufferedImage image = ImageIO.read(file);
                if (image == null) throw new IllegalArgumentException("That file is not a supported image.");
                poster[0] = java.nio.file.Files.readAllBytes(file.toPath());
                posterName.setText(file.getName());
            } catch (Exception ex) {
                error(ex.getMessage() == null ? "Could not read that photo." : ex.getMessage());
            }
        });

        add.addActionListener(e -> {
            try {
                String value = title.getText().trim();
                if (value.isEmpty()) throw new IllegalArgumentException("Title is required.");
                int minutes = Integer.parseInt(duration.getText().trim());
                if (minutes <= 0) throw new NumberFormatException();
                if (editingId[0] == 0) {
                    gateway.addMovie(value, minutes, genre.getText().trim(), poster[0], trailer.getText().trim());
                } else {
                    gateway.updateMovie(editingId[0], value, minutes, genre.getText().trim(),
                            poster[0], trailer.getText().trim());
                }
                clearMovieEditor(editingId, title, duration, genre, trailer, poster, posterName, add, cancel);
                refresh();
            } catch (NumberFormatException ex) {
                error("Duration must be a positive whole number of minutes.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        edit.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a movie to edit."); return; }
            Movie movie = gateway.movies().stream().filter(item -> item.id() == id).findFirst().orElse(null);
            if (movie == null) { error("That movie is no longer available."); refresh(); return; }
            editingId[0] = id;
            title.setText(movie.title());
            duration.setText(String.valueOf(movie.duration()));
            genre.setText(movie.genre());
            trailer.setText(movie.trailerUrl());
            poster[0] = movie.poster();
            posterName.setText(movie.poster() == null ? "No photo" : "Current photo");
            add.setText("Save changes");
            cancel.setEnabled(true);
        });
        cancel.addActionListener(e -> clearMovieEditor(editingId, title, duration, genre, trailer,
                poster, posterName, add, cancel));
        del.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a movie first."); return; }
            if (!confirmDelete("Delete this movie and all of its shows? Booked shows cannot be deleted.")) return;
            try {
                gateway.deleteMovie(id);
                if (editingId[0] == id) {
                    clearMovieEditor(editingId, title, duration, genre, trailer, poster, posterName, add, cancel);
                }
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        return page("Movies", "Add a title, optional poster, and a YouTube trailer link.", table,
                row("Title", title, "Duration", duration, "Genre", genre,
                        "Trailer URL", trailer, pickPhoto, posterName, add, edit, cancel, del));
    }

    private JPanel buildShows() {
        JTable table = table(showModel);
        JTextField date = field(10);
        date.setText(LocalDate.now().toString());
        JTextField time = field(6);
        time.setText("18:30");
        JButton add = UiTheme.primaryButton("Schedule show");
        JButton edit = UiTheme.ghostButton("Edit selected");
        JButton cancel = UiTheme.ghostButton("Cancel");
        JButton del = UiTheme.ghostButton("Delete selected");
        int[] editingId = {0};
        add.addActionListener(e -> {
            Movie movie = (Movie) showMovieBox.getSelectedItem();
            Screen screen = (Screen) screenBox.getSelectedItem();
            if (movie == null || screen == null) {
                error("Add a movie, theatre, and screen first.");
                return;
            }
            try {
                LocalDate showDate = LocalDate.parse(date.getText().trim());
                LocalTime showTime = LocalTime.parse(time.getText().trim());
                if (editingId[0] == 0) gateway.addShow(movie.id(), screen.id(), showDate, showTime);
                else gateway.updateShow(editingId[0], movie.id(), showDate, showTime);
                editingId[0] = 0;
                add.setText("Schedule show");
                cancel.setEnabled(false);
                date.setText(LocalDate.now().toString());
                time.setText("18:30");
                screenBox.setEnabled(true);
                refresh();
            } catch (DateTimeParseException ex) {
                error("Use date format yyyy-MM-dd and time format HH:mm.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        edit.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a showtime to edit."); return; }
            Show show = gateway.shows().stream().filter(item -> item.id() == id).findFirst().orElse(null);
            if (show == null) { error("That showtime is no longer available."); refresh(); return; }
            editingId[0] = id;
            selectMovie(show.movieId());
            date.setText(show.date().toString());
            time.setText(show.time().toString());
            selectScreen(show.screenId());
            screenBox.setEnabled(false);
            add.setText("Save changes");
            cancel.setEnabled(true);
        });
        cancel.addActionListener(e -> {
            editingId[0] = 0;
            date.setText(LocalDate.now().toString());
            time.setText("18:30");
            screenBox.setEnabled(true);
            add.setText("Schedule show");
            cancel.setEnabled(false);
        });
        del.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a showtime first."); return; }
            if (!confirmDelete("Delete this showtime and its seats? Booked shows cannot be deleted.")) return;
            try {
                gateway.deleteShow(id);
                if (editingId[0] == id) {
                    editingId[0] = 0;
                    date.setText(LocalDate.now().toString());
                    time.setText("18:30");
                    screenBox.setEnabled(true);
                    add.setText("Schedule show");
                    cancel.setEnabled(false);
                }
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        cancel.setEnabled(false);
        return page("Showtimes", "Edit movie, date, or time. The screen stays fixed to preserve existing seat assignments.", table,
                row("Movie", showMovieBox, "Screen", screenBox, "Date", date, "Time", time, add, edit, cancel, del));
    }

    private JPanel buildTheatres() {
        JTable table = table(theatreModel);
        JTextField name = field(12);
        JTextField location = field(12);
        JTextField city = field(10);
        JButton add = UiTheme.primaryButton("Add theatre");
        JButton edit = UiTheme.ghostButton("Edit selected");
        JButton cancel = UiTheme.ghostButton("Cancel");
        JButton del = UiTheme.ghostButton("Delete selected");
        int[] editingId = {0};
        cancel.setEnabled(false);
        add.addActionListener(e -> {
            try {
                if (name.getText().trim().isEmpty() || city.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Theatre name and city are required.");
                }
                if (editingId[0] == 0) {
                    gateway.addTheatre(name.getText().trim(), location.getText().trim(), city.getText().trim());
                } else {
                    gateway.updateTheatre(editingId[0], name.getText().trim(),
                            location.getText().trim(), city.getText().trim());
                }
                editingId[0] = 0;
                name.setText("");
                location.setText("");
                city.setText("");
                add.setText("Add theatre");
                cancel.setEnabled(false);
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        edit.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a theatre to edit."); return; }
            Theatre theatre = gateway.theatres().stream().filter(item -> item.id() == id).findFirst().orElse(null);
            if (theatre == null) { error("That theatre is no longer available."); refresh(); return; }
            editingId[0] = id;
            name.setText(theatre.name());
            location.setText(theatre.location());
            city.setText(theatre.city());
            add.setText("Save changes");
            cancel.setEnabled(true);
        });
        cancel.addActionListener(e -> {
            editingId[0] = 0;
            name.setText("");
            location.setText("");
            city.setText("");
            add.setText("Add theatre");
            cancel.setEnabled(false);
        });
        del.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a theatre first."); return; }
            if (!confirmDelete("Delete this theatre, its screens, and their shows? Booked shows cannot be deleted.")) return;
            try {
                gateway.deleteTheatre(id);
                if (editingId[0] == id) {
                    editingId[0] = 0;
                    name.setText("");
                    location.setText("");
                    city.setText("");
                    add.setText("Add theatre");
                    cancel.setEnabled(false);
                }
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        return page("Theatres", "Locations that appear on the showtime listing.", table,
                row("Name", name, "Location", location, "City", city, add, edit, cancel, del));
    }

    private JPanel buildScreens() {
        JTable table = table(screenModel);
        JTextField name = field(10);
        JTextField seats = field(5);
        seats.setText("80");
        JButton add = UiTheme.primaryButton("Add screen");
        JButton edit = UiTheme.ghostButton("Edit selected");
        JButton cancel = UiTheme.ghostButton("Cancel");
        JButton del = UiTheme.ghostButton("Delete selected");
        int[] editingId = {0};
        cancel.setEnabled(false);
        add.addActionListener(e -> {
            Theatre theatre = (Theatre) theatreBox.getSelectedItem();
            if (theatre == null) { error("Add a theatre first."); return; }
            try {
                if (name.getText().trim().isEmpty()) {
                    error("Screen name is required.");
                    return;
                }
                int total = Integer.parseInt(seats.getText().trim());
                if (total <= 0) throw new NumberFormatException();
                if (editingId[0] == 0) gateway.addScreen(theatre.id(), name.getText().trim(), total);
                else gateway.updateScreen(editingId[0], theatre.id(), name.getText().trim(), total);
                editingId[0] = 0;
                name.setText("");
                seats.setText("80");
                add.setText("Add screen");
                cancel.setEnabled(false);
                refresh();
            } catch (NumberFormatException ex) {
                error("Total seats must be a positive whole number.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        edit.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a screen to edit."); return; }
            Screen screen = gateway.screens().stream().filter(item -> item.id() == id).findFirst().orElse(null);
            if (screen == null) { error("That screen is no longer available."); refresh(); return; }
            editingId[0] = id;
            selectTheatre(screen.theatreId());
            name.setText(screen.name());
            seats.setText(String.valueOf(screen.totalSeats()));
            add.setText("Save changes");
            cancel.setEnabled(true);
        });
        cancel.addActionListener(e -> {
            editingId[0] = 0;
            name.setText("");
            seats.setText("80");
            add.setText("Add screen");
            cancel.setEnabled(false);
        });
        del.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a screen first."); return; }
            if (!confirmDelete("Delete this screen and its shows? Booked shows cannot be deleted.")) return;
            try {
                gateway.deleteScreen(id);
                if (editingId[0] == id) {
                    editingId[0] = 0;
                    name.setText("");
                    seats.setText("80");
                    add.setText("Add screen");
                    cancel.setEnabled(false);
                }
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        return page("Screens", "Seat-count changes apply to newly scheduled shows; existing seat maps stay unchanged.", table,
                row("Theatre", theatreBox, "Screen name", name, "Total seats", seats, add, edit, cancel, del));
    }

    private JPanel buildPricing() {
        JTable table = table(priceModel);
        JTextField name = field(10);
        JTextField price = field(6);
        JButton add = UiTheme.primaryButton("Add category");
        JButton edit = UiTheme.ghostButton("Edit selected");
        JButton cancel = UiTheme.ghostButton("Cancel");
        JButton del = UiTheme.ghostButton("Delete selected");
        int[] editingId = {0};
        cancel.setEnabled(false);
        add.addActionListener(e -> {
            try {
                if (name.getText().trim().isEmpty()) throw new IllegalArgumentException("Category name is required.");
                BigDecimal amount = new BigDecimal(price.getText().trim());
                if (amount.signum() < 0) throw new NumberFormatException();
                if (editingId[0] == 0) gateway.addCategory(name.getText().trim(), amount);
                else gateway.updateCategory(editingId[0], name.getText().trim(), amount);
                editingId[0] = 0;
                name.setText("");
                price.setText("");
                add.setText("Add category");
                cancel.setEnabled(false);
                refresh();
            } catch (NumberFormatException ex) {
                error("Price must be a number, for example 200 or 199.00.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        edit.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a pricing category to edit."); return; }
            SeatCategory category = gateway.categories().stream()
                    .filter(item -> item.id() == id).findFirst().orElse(null);
            if (category == null) { error("That pricing category is no longer available."); refresh(); return; }
            editingId[0] = id;
            name.setText(category.name());
            price.setText(category.price().toPlainString());
            add.setText("Save changes");
            cancel.setEnabled(true);
        });
        cancel.addActionListener(e -> {
            editingId[0] = 0;
            name.setText("");
            price.setText("");
            add.setText("Add category");
            cancel.setEnabled(false);
        });
        del.addActionListener(e -> {
            int id = selectedId(table);
            if (id < 0) { error("Select a pricing category first."); return; }
            if (!confirmDelete("Delete this pricing category? Categories used by seats cannot be deleted.")) return;
            try {
                gateway.deleteCategory(id);
                if (editingId[0] == id) {
                    editingId[0] = 0;
                    name.setText("");
                    price.setText("");
                    add.setText("Add category");
                    cancel.setEnabled(false);
                }
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        return page("Seat pricing", "Platinum, Gold, and Silver tiers used on the seat map.", table,
                row("Category", name, "Price", price, add, edit, cancel, del));
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
        JPanel page = UiTheme.gradientPanel(new BorderLayout(0, 16));
        UiTheme.pad(page, 24, 26, 22, 26);
        JLabel heading = UiTheme.heading(title, 20);
        JLabel sub = UiTheme.muted(subtitle);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new javax.swing.BoxLayout(titles, javax.swing.BoxLayout.Y_AXIS));
        titles.add(heading);
        titles.add(javax.swing.Box.createVerticalStrut(5));
        titles.add(sub);
        top.add(titles, BorderLayout.WEST);
        page.add(top, BorderLayout.NORTH);
        page.add(UiTheme.scroll(table), BorderLayout.CENTER);
        form.setOpaque(false);
        JPanel glassForm = UiTheme.glassPanel(new BorderLayout());
        glassForm.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255, 28), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        glassForm.add(form, BorderLayout.CENTER);
        page.add(glassForm, BorderLayout.SOUTH);
        return page;
    }

    private static JPanel row(Object... parts) {
        JPanel form = new JPanel(new WrapLayout(FlowLayout.LEFT, 14, 10));
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

    private static int selectedId(JTable table) {
        int selected = table.getSelectedRow();
        if (selected < 0) return -1;
        return (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(selected), 0);
    }

    private void selectMovie(int id) {
        for (int i = 0; i < showMovieBox.getItemCount(); i++) {
            Movie movie = showMovieBox.getItemAt(i);
            if (movie.id() == id) {
                showMovieBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectScreen(int id) {
        for (int i = 0; i < screenBox.getItemCount(); i++) {
            Screen screen = screenBox.getItemAt(i);
            if (screen.id() == id) {
                screenBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectTheatre(int id) {
        for (int i = 0; i < theatreBox.getItemCount(); i++) {
            Theatre theatre = theatreBox.getItemAt(i);
            if (theatre.id() == id) {
                theatreBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private static void clearMovieEditor(int[] editingId, JTextField title, JTextField duration,
                                         JTextField genre, JTextField trailer, byte[][] poster,
                                         JLabel posterName, JButton save, JButton cancel) {
        editingId[0] = 0;
        title.setText("");
        duration.setText("");
        genre.setText("");
        trailer.setText("");
        poster[0] = null;
        posterName.setText("No photo");
        save.setText("Add movie");
        cancel.setEnabled(false);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this,
                message == null || message.isBlank() ? "An unexpected error occurred." : message,
                "Could not complete action", JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirmDelete(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirm deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
