package ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Single seam between the Swing UI and the JDBC backend.
 */
public interface CinemaGateway {

    record User(int id, String name, String role) {
        public boolean isAdmin() { return "Admin".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role); }
    }

    record Movie(int id, String title, int duration, String genre, byte[] poster, String trailerUrl) {
        @Override public String toString() { return title; }
    }

    record Theatre(int id, String name, String location, String city) {
        @Override public String toString() {
            return city == null || city.isBlank() ? name : name + "  ·  " + city;
        }
    }

    record Screen(int id, int theatreId, String name, String theatreName, int totalSeats) {
        @Override public String toString() { return theatreName + "  ·  " + name; }
    }

    record Show(int id, int movieId, String movieTitle, int screenId, String theatreName,
                String screenName, LocalDate date, LocalTime time) {
        public String screenLabel() { return theatreName + "  ·  " + screenName; }

        @Override public String toString() {
            return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + "   "
                    + time.format(DateTimeFormatter.ofPattern("HH:mm")) + "   ·   " + screenLabel();
        }
    }

    record Seat(int id, int showId, String category, BigDecimal price, String row, int number, boolean booked) {
        public String label() { return row + number; }
    }

    record SeatCategory(int id, String name, BigDecimal price) {
        @Override public String toString() { return name + "  ·  Rs " + price; }
    }

    record Booking(int id, int showId, String movieTitle, String seatLabel, String clerkName,
                   String customerName, BigDecimal amount, LocalDateTime bookedAt) {}

    /** Returns the user, or null if credentials are wrong. */
    User login(String username, String password);

    User register(String name, String username, String password, String role);

    List<Movie> movies();
    void addMovie(String title, int durationMinutes, String genre, byte[] poster, String trailerUrl);
    void updateMovie(int movieId, String title, int durationMinutes, String genre, byte[] poster, String trailerUrl);
    void deleteMovie(int movieId);

    List<Theatre> theatres();
    void addTheatre(String name, String location, String city);
    void updateTheatre(int theatreId, String name, String location, String city);
    void deleteTheatre(int theatreId);

    List<Screen> screens();
    void addScreen(int theatreId, String name, int totalSeats);
    void updateScreen(int screenId, int theatreId, String name, int totalSeats);
    void deleteScreen(int screenId);

    List<SeatCategory> categories();
    void addCategory(String name, BigDecimal price);
    void updateCategory(int categoryId, String name, BigDecimal price);

    List<Show> shows();
    List<Show> showsForMovie(int movieId);
    void addShow(int movieId, int screenId, LocalDate date, LocalTime time);
    void updateShow(int showId, int movieId, LocalDate date, LocalTime time);

    List<Seat> seats(int showId);

    /** Must be one transaction; throw an Exception if any seat is already booked. */
    List<Booking> bookSeats(int showId, List<Integer> seatIds, int clerkId, String customerName) throws Exception;

    List<Booking> history();
}
