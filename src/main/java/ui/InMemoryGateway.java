package ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Demo backend so the UI can run without MySQL. */
public class InMemoryGateway implements CinemaGateway {

    private final List<Movie> movies = new ArrayList<>();
    private final List<Theatre> theatres = new ArrayList<>();
    private final List<Screen> screens = new ArrayList<>();
    private final List<Show> shows = new ArrayList<>();
    private final List<SeatCategory> categories = new ArrayList<>();
    private final Map<Integer, List<Seat>> seats = new HashMap<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final Map<Integer, User> usersById = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, String> passwords = new HashMap<>();
    private int movieSeq = 1, theatreSeq = 1, screenSeq = 1, showSeq = 1, seatSeq = 1, bookingSeq = 1, userSeq = 3;

    public InMemoryGateway() {
        User admin = new User(1, "Administrator", "Admin");
        User clerk = new User(2, "Ticket Clerk", "Clerk");
        usersById.put(1, admin);
        usersById.put(2, clerk);
        usersByUsername.put("admin", admin);
        usersByUsername.put("clerk", clerk);
        passwords.put("admin", "admin123");
        passwords.put("clerk", "clerk123");
        categories.add(new SeatCategory(1, "Platinum", new BigDecimal("300")));
        categories.add(new SeatCategory(2, "Gold", new BigDecimal("200")));
        categories.add(new SeatCategory(3, "Silver", new BigDecimal("120")));
        addTheatre("PVR City Mall", "FC Road", "Pune");
        addTheatre("INOX Phoenix", "Viman Nagar", "Pune");
        addScreen(1, "Screen 1", 80);
        addScreen(1, "Screen 2", 80);
        addScreen(2, "Audi 1", 80);
        addMovie("Interstellar", 169, "Sci-Fi");
        addMovie("The Dark Knight", 152, "Action");
        addMovie("La La Land", 128, "Romance");
        addShow(1, 1, LocalDate.now(), LocalTime.of(18, 30));
        addShow(1, 2, LocalDate.now(), LocalTime.of(21, 30));
        addShow(2, 3, LocalDate.now(), LocalTime.of(19, 0));
        addShow(3, 1, LocalDate.now().plusDays(1), LocalTime.of(16, 15));
    }

    @Override
    public synchronized User login(String username, String password) {
        String expected = passwords.get(username);
        if (expected == null || !expected.equals(password)) return null;
        return usersByUsername.get(username);
    }

    @Override
    public synchronized User register(String name, String username, String password, String role) {
        if (passwords.containsKey(username)) throw new RuntimeException("That username is already taken.");
        boolean admin = "Admin".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
        User user = new User(userSeq++, name, admin ? "Admin" : "Clerk");
        usersById.put(user.id(), user);
        usersByUsername.put(username, user);
        passwords.put(username, password);
        return user;
    }

    @Override public synchronized List<Movie> movies() { return List.copyOf(movies); }

    @Override
    public synchronized void addMovie(String title, int duration, String genre) {
        movies.add(new Movie(movieSeq++, title, duration, genre));
    }

    @Override
    public synchronized void deleteMovie(int id) {
        movies.removeIf(movie -> movie.id() == id);
        shows.removeIf(show -> {
            if (show.movieId() == id) {
                seats.remove(show.id());
                return true;
            }
            return false;
        });
    }

    @Override public synchronized List<Theatre> theatres() { return List.copyOf(theatres); }

    @Override
    public synchronized void addTheatre(String name, String location, String city) {
        theatres.add(new Theatre(theatreSeq++, name, location, city));
    }

    @Override
    public synchronized void deleteTheatre(int theatreId) {
        theatres.removeIf(theatre -> theatre.id() == theatreId);
        screens.removeIf(screen -> screen.theatreId() == theatreId);
    }

    @Override public synchronized List<Screen> screens() { return List.copyOf(screens); }

    @Override
    public synchronized void addScreen(int theatreId, String name, int totalSeats) {
        Theatre theatre = theatres.stream().filter(t -> t.id() == theatreId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Theatre not found."));
        screens.add(new Screen(screenSeq++, theatreId, name, theatre.name(), totalSeats));
    }

    @Override
    public synchronized void deleteScreen(int screenId) {
        screens.removeIf(screen -> screen.id() == screenId);
    }

    @Override public synchronized List<SeatCategory> categories() { return List.copyOf(categories); }

    @Override
    public synchronized void addCategory(String name, BigDecimal price) {
        categories.add(new SeatCategory(categories.size() + 1, name, price));
    }

    @Override public synchronized List<Show> shows() { return List.copyOf(shows); }

    @Override
    public synchronized List<Show> showsForMovie(int movieId) {
        return shows.stream().filter(show -> show.movieId() == movieId).toList();
    }

    @Override
    public synchronized void addShow(int movieId, int screenId, LocalDate date, LocalTime time) {
        Movie movie = movies.stream().filter(item -> item.id() == movieId).findFirst().orElseThrow();
        Screen screen = screens.stream().filter(item -> item.id() == screenId).findFirst().orElseThrow();
        int showId = showSeq++;
        shows.add(new Show(showId, movieId, movie.title(), screenId, screen.theatreName(), screen.name(), date, time));
        List<Seat> list = new ArrayList<>();
        String rows = "ABCDEFGH";
        for (int r = 0; r < rows.length(); r++) {
            SeatCategory category = categories.get(Math.min(categories.size() - 1, r * categories.size() / rows.length()));
            for (int n = 1; n <= 10; n++) {
                list.add(new Seat(seatSeq++, showId, category.name(), category.price(),
                        String.valueOf(rows.charAt(r)), n, false));
            }
        }
        seats.put(showId, list);
    }

    @Override
    public synchronized List<Seat> seats(int showId) {
        return List.copyOf(seats.getOrDefault(showId, List.of()));
    }

    @Override
    public synchronized List<Booking> bookSeats(int showId, List<Integer> ids, int clerkId, String customer)
            throws Exception {
        List<Seat> list = seats.get(showId);
        if (list == null) throw new IllegalStateException("Show not found");
        for (int id : ids) {
            for (Seat seat : list) {
                if (seat.id() == id && seat.booked()) {
                    throw new IllegalStateException("Seat " + seat.label() + " was just booked by someone else.");
                }
            }
        }
        String title = shows.stream().filter(show -> show.id() == showId).findFirst().orElseThrow().movieTitle();
        List<Booking> made = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Seat seat = list.get(i);
            if (ids.contains(seat.id())) {
                list.set(i, new Seat(seat.id(), seat.showId(), seat.category(), seat.price(),
                        seat.row(), seat.number(), true));
                Booking booking = new Booking(bookingSeq++, showId, title, seat.label(),
                        usersById.get(clerkId).name(), customer, seat.price(), LocalDateTime.now());
                bookings.add(booking);
                made.add(booking);
            }
        }
        return made;
    }

    @Override
    public synchronized List<Booking> history() {
        List<Booking> copy = new ArrayList<>(bookings);
        Collections.reverse(copy);
        return copy;
    }
}
