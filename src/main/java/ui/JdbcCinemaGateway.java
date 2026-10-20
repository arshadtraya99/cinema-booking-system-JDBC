package ui;

import configuration.DBconnection;
import daoImplementation.BookingDaoImpl;
import daoImplementation.MovieDaoImpl;
import daoImplementation.ScreenDaoImpl;
import daoImplementation.SeatCategoryDaoImpl;
import daoImplementation.SeatDaoImpl;
import daoImplementation.ShowDaoImpl;
import daoImplementation.TheatreDaoImpl;
import daoImplementation.UserDaoImpl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** DAO-backed gateway used by the Swing UI. */
public class JdbcCinemaGateway implements CinemaGateway {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserDaoImpl users = new UserDaoImpl();
    private final MovieDaoImpl movies = new MovieDaoImpl();
    private final TheatreDaoImpl theatres = new TheatreDaoImpl();
    private final ScreenDaoImpl screens = new ScreenDaoImpl();
    private final ShowDaoImpl shows = new ShowDaoImpl();
    private final SeatDaoImpl seats = new SeatDaoImpl();
    private final SeatCategoryDaoImpl categories = new SeatCategoryDaoImpl();
    private final BookingDaoImpl bookings = new BookingDaoImpl();

    public JdbcCinemaGateway() {
        ensureMoviePosterColumn();
        ensureMovieTrailerColumn();
        ensureDefaultCategories();
    }

    @Override
    public User login(String username, String password) {
        try {
            model.User found = users.login(username, password);
            return found == null ? null : toUser(found);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to sign in: " + e.getMessage(), e);
        }
    }

    @Override
    public User register(String name, String username, String password, String role) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password is required.");
        String dbRole = "Admin".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) ? "ADMIN" : "CLERK";
        model.User user = new model.User(0, name.trim(), username.trim(), password, dbRole);
        try {
            if (!users.addUser(user)) throw new RuntimeException("Could not create the account.");
            return toUser(user);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                throw new RuntimeException("That username is already taken.");
            }
            throw new RuntimeException("Could not create the account: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Movie> movies() {
        List<Movie> out = new ArrayList<>();
        for (model.Movie movie : movies.getMovies()) out.add(toMovie(movie));
        return out;
    }

    @Override
    public void addMovie(String title, int durationMinutes, String genre, byte[] poster, String trailerUrl) {
        model.Movie movie = new model.Movie();
        movie.setTitle(title);
        movie.setDuration(durationMinutes);
        movie.setGenre(genre);
        movie.setPoster(poster);
        movie.setTrailerUrl(blankToNull(trailerUrl));
        if (!movies.addMovie(movie)) throw new RuntimeException("Could not add the movie.");
    }

    @Override
    public void updateMovie(int movieId, String title, int durationMinutes, String genre,
                           byte[] poster, String trailerUrl) {
        model.Movie movie = new model.Movie(movieId, title, durationMinutes, genre, poster, blankToNull(trailerUrl));
        if (!movies.updateMovie(movie)) throw new RuntimeException("Could not update the movie.");
    }

    @Override
    public void deleteMovie(int movieId) {
        if (!movies.deleteMovie(movieId)) {
            throw new RuntimeException("Could not delete this movie. Cancel its bookings first.");
        }
    }

    @Override
    public List<Theatre> theatres() {
        List<Theatre> out = new ArrayList<>();
        for (model.Theatre theatre : theatres.getAllTheatres()) out.add(toTheatre(theatre));
        return out;
    }

    @Override
    public void addTheatre(String name, String location, String city) {
        model.Theatre theatre = new model.Theatre();
        theatre.setName(name);
        theatre.setLocation(location);
        theatre.setCity(city);
        if (!theatres.addTheatre(theatre)) throw new RuntimeException("Could not add the theatre.");
    }

    @Override
    public void updateTheatre(int theatreId, String name, String location, String city) {
        model.Theatre theatre = new model.Theatre(theatreId, name, location, city);
        if (!theatres.updateTheatre(theatre)) throw new RuntimeException("Could not update the theatre.");
    }

    @Override
    public void deleteTheatre(int theatreId) {
        if (!theatres.deleteTheatre(theatreId)) throw new RuntimeException("Could not delete the theatre.");
    }

    @Override
    public List<Screen> screens() {
        List<Screen> out = new ArrayList<>();
        for (model.Screen screen : screens.getAllScreens()) out.add(toScreen(screen));
        return out;
    }

    @Override
    public void addScreen(int theatreId, String name, int totalSeats) {
        model.Screen screen = new model.Screen();
        screen.setTheatreId(theatreId);
        screen.setName(name);
        screen.setTotalSeats(totalSeats);
        if (!screens.addScreen(screen)) throw new RuntimeException("Could not add the screen.");
    }

    @Override
    public void updateScreen(int screenId, int theatreId, String name, int totalSeats) {
        model.Screen screen = new model.Screen(screenId, theatreId, name, totalSeats);
        if (!screens.updateScreen(screen)) throw new RuntimeException("Could not update the screen.");
    }

    @Override
    public void deleteScreen(int screenId) {
        if (!screens.deleteScreen(screenId)) throw new RuntimeException("Could not delete the screen.");
    }

    @Override
    public List<SeatCategory> categories() {
        List<SeatCategory> out = new ArrayList<>();
        for (model.SeatCategory category : categories.getAllSeatCategories()) {
            out.add(new SeatCategory(category.getId(), category.getCategoryName(),
                    BigDecimal.valueOf(category.getPrice())));
        }
        return out;
    }

    @Override
    public void addCategory(String name, BigDecimal price) {
        model.SeatCategory category = new model.SeatCategory();
        category.setCategoryName(name);
        category.setPrice(price.doubleValue());
        if (!categories.addSeatCategory(category)) throw new RuntimeException("Could not add the price tier.");
    }

    @Override
    public void updateCategory(int categoryId, String name, BigDecimal price) {
        model.SeatCategory category = new model.SeatCategory(categoryId, name, price.doubleValue());
        if (!categories.updateSeatCategory(category)) throw new RuntimeException("Could not update the price tier.");
    }

    @Override
    public List<Show> shows() {
        List<Show> out = new ArrayList<>();
        for (model.Show show : shows.getAllShows()) out.add(toShow(show));
        return out;
    }

    @Override
    public List<Show> showsForMovie(int movieId) {
        List<Show> out = new ArrayList<>();
        for (model.Show show : shows.getShowsByMovieId(movieId)) out.add(toShow(show));
        return out;
    }

    @Override
    public void addShow(int movieId, int screenId, LocalDate date, LocalTime time) {
        model.Show show = new model.Show();
        show.setMovieId(movieId);
        show.setScreenId(screenId);
        show.setShowDate(date.toString());
        show.setShowTime(time.toString());
        if (!shows.addShow(show)) {
            throw new RuntimeException("Could not schedule this show. The screen may already have a slot then.");
        }
        generateSeats(show.getId(), screenId);
    }

    @Override
    public void updateShow(int showId, int movieId, LocalDate date, LocalTime time) {
        model.Show show = shows.getShowById(showId);
        if (show == null) throw new RuntimeException("Show not found.");
        show.setMovieId(movieId);
        show.setShowDate(date.toString());
        show.setShowTime(time.toString());
        if (!shows.updateShow(show)) {
            throw new RuntimeException("Could not update this show. The screen may already have a slot then.");
        }
    }

    @Override
    public List<Seat> seats(int showId) {
        List<Seat> out = new ArrayList<>();
        for (model.Seat seat : seats.getSeatsByShowId(showId)) {
            model.SeatCategory category = categories.getSeatCategoryById(seat.getCategoryId());
            String name = category == null ? "Standard" : category.getCategoryName();
            BigDecimal price = category == null ? BigDecimal.ZERO : BigDecimal.valueOf(category.getPrice());
            out.add(new Seat(seat.getId(), seat.getShowId(), name, price,
                    seat.getSeatRow(), seat.getNumber(), seat.isBooked()));
        }
        return out;
    }

    @Override
    public List<Booking> bookSeats(int showId, List<Integer> seatIds, int clerkId, String customerName)
            throws Exception {
        if (seatIds == null || seatIds.isEmpty()) throw new IllegalArgumentException("Select at least one seat.");
        Show show = toShow(shows.getShowById(showId));
        model.User clerk = users.getUserById(clerkId);
        String clerkName = clerk == null ? "Clerk" : clerk.getName();
        List<Booking> made = new ArrayList<>();

        try (Connection connection = DBconnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (int seatId : seatIds) {
                    SeatInfo info = lockSeat(connection, showId, seatId);
                    int bookingId = insertBooking(connection, showId, seatId, clerkId, customerName, info.price);
                    markBooked(connection, seatId);
                    made.add(new Booking(bookingId, showId, show.movieTitle(), info.label, clerkName,
                            customerName, info.price, LocalDateTime.now()));
                }
                connection.commit();
                return made;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<Booking> history() {
        List<Booking> out = new ArrayList<>();
        for (model.Booking booking : bookings.getAllBookings()) {
            model.Show show = shows.getShowById(booking.getShowId());
            Show mapped = show == null ? null : toShow(show);
            model.Seat seat = seats.getSeatById(booking.getSeatId());
            model.User clerk = users.getUserById(booking.getClerkId());
            String seatLabel = seat == null ? ("#" + booking.getSeatId()) : seat.getSeatRow() + seat.getNumber();
            out.add(new Booking(
                    booking.getId(),
                    booking.getShowId(),
                    mapped == null ? "Movie" : mapped.movieTitle(),
                    seatLabel,
                    clerk == null ? "Clerk" : clerk.getName(),
                    booking.getCustomerName(),
                    BigDecimal.valueOf(booking.getTotalAmount()),
                    parseDateTime(booking.getBookingTime())
            ));
        }
        return out;
    }

    private void generateSeats(int showId, int screenId) {
        ensureDefaultCategories();
        List<model.SeatCategory> tiers = categories.getAllSeatCategories();
        if (tiers.isEmpty()) throw new RuntimeException("Add at least one seat category before scheduling shows.");

        model.Screen screen = screens.getScreenById(screenId);
        int total = screen == null || screen.getTotalSeats() <= 0 ? 80 : screen.getTotalSeats();
        int cols = 10;
        int rows = Math.max(4, (int) Math.ceil(total / (double) cols));
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int created = 0;
        for (int r = 0; r < rows && created < total; r++) {
            model.SeatCategory tier = tiers.get(Math.min(tiers.size() - 1, r * tiers.size() / rows));
            for (int n = 1; n <= cols && created < total; n++) {
                model.Seat seat = new model.Seat();
                seat.setShowId(showId);
                seat.setCategoryId(tier.getId());
                seat.setSeatRow(String.valueOf(letters.charAt(r)));
                seat.setNumber(n);
                seat.setBooked(false);
                seats.addSeat(seat);
                created++;
            }
        }
    }

    private void ensureDefaultCategories() {
        if (!categories.getAllSeatCategories().isEmpty()) return;
        addCategory("Platinum", new BigDecimal("300.00"));
        addCategory("Gold", new BigDecimal("200.00"));
        addCategory("Silver", new BigDecimal("120.00"));
    }

    private SeatInfo lockSeat(Connection connection, int showId, int seatId) throws SQLException {
        String sql = "SELECT s.SHOW_ID, s.ISBOOKED, s.SEAT_ROW, s.`NUMBER`, c.PRICE "
                + "FROM SEATS s LEFT JOIN SEAT_CATEGORY c ON c.CATEGORY_ID = s.CATEGORY_ID "
                + "WHERE s.SEAT_ID = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seatId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()
                        || resultSet.getInt("SHOW_ID") != showId
                        || resultSet.getBoolean("ISBOOKED")) {
                    throw new IllegalStateException("Seat was just booked by someone else. Pick another.");
                }
                BigDecimal price = resultSet.getBigDecimal("PRICE");
                if (price == null) price = BigDecimal.ZERO;
                String label = resultSet.getString("SEAT_ROW") + resultSet.getInt("NUMBER");
                return new SeatInfo(label, price);
            }
        }
    }

    private int insertBooking(Connection connection, int showId, int seatId, int clerkId,
                              String customerName, BigDecimal price) throws SQLException {
        String sql = "INSERT INTO BOOKINGS (SHOW_ID, SEAT_ID, CLERK_ID, CUSTOMER_NAME, TOTAL_AMOUNT) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, showId);
            statement.setInt(2, seatId);
            statement.setInt(3, clerkId);
            statement.setString(4, customerName);
            statement.setBigDecimal(5, price);
            if (statement.executeUpdate() != 1) throw new SQLException("Could not save the booking.");
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Could not save the booking.");
    }

    private void markBooked(Connection connection, int seatId) throws SQLException {
        String sql = "UPDATE SEATS SET ISBOOKED = TRUE WHERE SEAT_ID = ? "
                + "AND (ISBOOKED = FALSE OR ISBOOKED IS NULL)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seatId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException("Seat was just booked by someone else. Pick another.");
            }
        }
    }

    private User toUser(model.User user) {
        String role = user.getRole();
        if ("ADMIN".equalsIgnoreCase(role)) role = "Admin";
        else if ("CLERK".equalsIgnoreCase(role)) role = "Clerk";
        return new User(user.getId(), user.getName(), role);
    }

    private Movie toMovie(model.Movie movie) {
        return new Movie(movie.getId(), movie.getTitle(), Math.round(movie.getDuration()),
                movie.getGenre() == null ? "" : movie.getGenre(), movie.getPoster(),
                movie.getTrailerUrl() == null ? "" : movie.getTrailerUrl());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void ensureMoviePosterColumn() {
        String exists = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'movies' "
                + "AND LOWER(COLUMN_NAME) = 'poster'";
        try (Connection connection = DBconnection.getConnection();
             PreparedStatement check = connection.prepareStatement(exists);
             ResultSet resultSet = check.executeQuery()) {
            if (resultSet.next() && resultSet.getInt(1) > 0) return;
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE MOVIES ADD COLUMN POSTER MEDIUMBLOB");
            }
        } catch (SQLException e) {
            System.err.println("Could not add movie poster column: " + e.getMessage());
        }
    }

    private void ensureMovieTrailerColumn() {
        String exists = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'movies' "
                + "AND LOWER(COLUMN_NAME) = 'trailer_url'";
        try (Connection connection = DBconnection.getConnection();
             PreparedStatement check = connection.prepareStatement(exists);
             ResultSet resultSet = check.executeQuery()) {
            if (resultSet.next() && resultSet.getInt(1) > 0) return;
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE MOVIES ADD COLUMN TRAILER_URL VARCHAR(500)");
            }
        } catch (SQLException e) {
            System.err.println("Could not add movie trailer column: " + e.getMessage());
        }
    }

    private Theatre toTheatre(model.Theatre theatre) {
        return new Theatre(theatre.getId(), theatre.getName(),
                theatre.getLocation() == null ? "" : theatre.getLocation(),
                theatre.getCity() == null ? "" : theatre.getCity());
    }

    private Screen toScreen(model.Screen screen) {
        model.Theatre theatre = theatres.getTheatreById(screen.getTheatreId());
        String theatreName = theatre == null ? "Theatre" : theatre.getName();
        return new Screen(screen.getId(), screen.getTheatreId(), screen.getName(),
                theatreName, screen.getTotalSeats());
    }

    private Show toShow(model.Show show) {
        if (show == null) throw new IllegalStateException("Show not found.");
        model.Movie movie = movies.getMovieById(show.getMovieId());
        model.Screen screen = screens.getScreenById(show.getScreenId());
        Screen mapped = screen == null
                ? new Screen(show.getScreenId(), 0, "Screen", "Theatre", 0)
                : toScreen(screen);
        String title = movie == null ? "Movie" : movie.getTitle();
        return new Show(show.getId(), show.getMovieId(), title, show.getScreenId(),
                mapped.theatreName(), mapped.name(), parseDate(show.getShowDate()), parseTime(show.getShowTime()));
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return LocalDate.now();
        String value = raw.trim();
        if (value.length() > 10) value = value.substring(0, 10);
        return LocalDate.parse(value);
    }

    private static LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) return LocalTime.MIDNIGHT;
        String value = raw.trim();
        if (value.length() >= 8) value = value.substring(0, 8);
        return LocalTime.parse(value);
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return LocalDateTime.now();
        String value = raw.trim().replace('T', ' ');
        if (value.length() >= 19) value = value.substring(0, 19);
        try {
            return LocalDateTime.parse(value, DATE_TIME);
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }

    private record SeatInfo(String label, BigDecimal price) {}
}
