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
    private final Map<Integer, Integer> seatCategoryIds = new HashMap<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final Map<Integer, User> usersById = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, String> passwords = new HashMap<>();
    private int movieSeq = 1, theatreSeq = 1, screenSeq = 1, showSeq = 1, seatSeq = 1,
            bookingSeq = 1, categorySeq = 4, userSeq = 3;

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
        addMovie("Interstellar", 169, "Sci-Fi", null, "https://www.youtube.com/watch?v=zSWdZVtXT7E");
        addMovie("The Dark Knight", 152, "Action", null, "https://www.youtube.com/watch?v=EXeTwQWrcwY");
        addMovie("La La Land", 128, "Romance", null, "https://www.youtube.com/watch?v=0pdqf4P9MB8");
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
    public synchronized void addMovie(String title, int duration, String genre, byte[] poster, String trailerUrl) {
        movies.add(new Movie(movieSeq++, title, duration, genre, poster, trailerUrl));
    }

    @Override
    public synchronized void updateMovie(int id, String title, int duration, String genre,
                                         byte[] poster, String trailerUrl) {
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            if (movie.id() == id) {
                movies.set(i, new Movie(id, title, duration, genre, poster, trailerUrl));
                for (int showIndex = 0; showIndex < shows.size(); showIndex++) {
                    Show show = shows.get(showIndex);
                    if (show.movieId() == id) {
                        shows.set(showIndex, new Show(show.id(), show.movieId(), title,
                                show.screenId(), show.theatreName(), show.screenName(), show.date(), show.time()));
                    }
                }
                return;
            }
        }
        throw new IllegalArgumentException("Movie not found.");
    }

    @Override
    public synchronized void deleteMovie(int id) {
        if (movies.stream().noneMatch(movie -> movie.id() == id)) throw new IllegalArgumentException("Movie not found.");
        deleteShows(shows.stream().filter(show -> show.movieId() == id).toList());
        movies.removeIf(movie -> movie.id() == id);
    }

    @Override public synchronized List<Theatre> theatres() { return List.copyOf(theatres); }

    @Override
    public synchronized void addTheatre(String name, String location, String city) {
        theatres.add(new Theatre(theatreSeq++, name, location, city));
    }

    @Override
    public synchronized void updateTheatre(int id, String name, String location, String city) {
        boolean found = false;
        for (int i = 0; i < theatres.size(); i++) {
            Theatre theatre = theatres.get(i);
            if (theatre.id() == id) {
                theatres.set(i, new Theatre(id, name, location, city));
                found = true;
                break;
            }
        }
        if (!found) throw new IllegalArgumentException("Theatre not found.");

        for (int i = 0; i < screens.size(); i++) {
            Screen screen = screens.get(i);
            if (screen.theatreId() == id) {
                screens.set(i, new Screen(screen.id(), id, screen.name(), name, screen.totalSeats()));
            }
        }
        for (int i = 0; i < shows.size(); i++) {
            Show show = shows.get(i);
            if (screens.stream().anyMatch(screen -> screen.id() == show.screenId())) {
                Screen screen = screens.stream().filter(item -> item.id() == show.screenId()).findFirst().orElseThrow();
                shows.set(i, new Show(show.id(), show.movieId(), show.movieTitle(), show.screenId(),
                        screen.theatreName(), screen.name(), show.date(), show.time()));
            }
        }
    }

    @Override
    public synchronized void deleteTheatre(int theatreId) {
        if (theatres.stream().noneMatch(theatre -> theatre.id() == theatreId)) {
            throw new IllegalArgumentException("Theatre not found.");
        }
        List<Integer> screenIds = screens.stream().filter(screen -> screen.theatreId() == theatreId)
                .map(Screen::id).toList();
        deleteShows(shows.stream().filter(show -> screenIds.contains(show.screenId())).toList());
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
    public synchronized void updateScreen(int id, int theatreId, String name, int totalSeats) {
        Theatre theatre = theatres.stream().filter(item -> item.id() == theatreId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Theatre not found."));
        boolean found = false;
        for (int i = 0; i < screens.size(); i++) {
            Screen screen = screens.get(i);
            if (screen.id() == id) {
                screens.set(i, new Screen(id, theatreId, name, theatre.name(), totalSeats));
                found = true;
                break;
            }
        }
        if (!found) throw new IllegalArgumentException("Screen not found.");
        for (int i = 0; i < shows.size(); i++) {
            Show show = shows.get(i);
            if (show.screenId() == id) {
                shows.set(i, new Show(show.id(), show.movieId(), show.movieTitle(), id,
                        theatre.name(), name, show.date(), show.time()));
            }
        }
    }

    @Override
    public synchronized void deleteScreen(int screenId) {
        if (screens.stream().noneMatch(screen -> screen.id() == screenId)) {
            throw new IllegalArgumentException("Screen not found.");
        }
        deleteShows(shows.stream().filter(show -> show.screenId() == screenId).toList());
        screens.removeIf(screen -> screen.id() == screenId);
    }

    @Override public synchronized List<SeatCategory> categories() { return List.copyOf(categories); }

    @Override
    public synchronized void addCategory(String name, BigDecimal price) {
        categories.add(new SeatCategory(categorySeq++, name, price));
    }

    @Override
    public synchronized void updateCategory(int id, String name, BigDecimal price) {
        boolean found = false;
        for (int i = 0; i < categories.size(); i++) {
            SeatCategory category = categories.get(i);
            if (category.id() == id) {
                categories.set(i, new SeatCategory(id, name, price));
                found = true;
                break;
            }
        }
        if (!found) throw new IllegalArgumentException("Seat category not found.");
        for (Map.Entry<Integer, List<Seat>> entry : seats.entrySet()) {
            List<Seat> updatedSeats = new ArrayList<>();
            for (Seat seat : entry.getValue()) {
                updatedSeats.add(seatCategoryIds.get(seat.id()) == id
                        ? new Seat(seat.id(), seat.showId(), name, price, seat.row(), seat.number(), seat.booked())
                        : seat);
            }
            entry.setValue(updatedSeats);
        }
    }

    @Override
    public synchronized void deleteCategory(int id) {
        SeatCategory category = categories.stream().filter(item -> item.id() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seat category not found."));
        if (seatCategoryIds.containsValue(id)) {
            throw new IllegalStateException("Could not delete this price tier. It is in use by seats.");
        }
        categories.removeIf(item -> item.id() == id);
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
        if (categories.isEmpty()) {
            addCategory("Platinum", new BigDecimal("300.00"));
            addCategory("Gold", new BigDecimal("200.00"));
            addCategory("Silver", new BigDecimal("120.00"));
        }
        int showId = showSeq++;
        shows.add(new Show(showId, movieId, movie.title(), screenId, screen.theatreName(), screen.name(), date, time));
        List<Seat> list = new ArrayList<>();
        String rows = "ABCDEFGH";
        for (int r = 0; r < rows.length(); r++) {
            SeatCategory category = categories.get(Math.min(categories.size() - 1, r * categories.size() / rows.length()));
            for (int n = 1; n <= 10; n++) {
                int seatId = seatSeq++;
                list.add(new Seat(seatId, showId, category.name(), category.price(),
                        String.valueOf(rows.charAt(r)), n, false));
                seatCategoryIds.put(seatId, category.id());
            }
        }
        seats.put(showId, list);
    }

    @Override
    public synchronized void updateShow(int id, int movieId, LocalDate date, LocalTime time) {
        Movie movie = movies.stream().filter(item -> item.id() == movieId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Movie not found."));
        for (int i = 0; i < shows.size(); i++) {
            Show show = shows.get(i);
            if (show.id() == id) {
                boolean slotTaken = shows.stream().anyMatch(other -> other.id() != id
                        && other.screenId() == show.screenId()
                        && other.date().equals(date)
                        && other.time().equals(time));
                if (slotTaken) throw new IllegalArgumentException("That screen already has a show at this time.");
                shows.set(i, new Show(id, movieId, movie.title(), show.screenId(),
                        show.theatreName(), show.screenName(), date, time));
                return;
            }
        }
        throw new IllegalArgumentException("Show not found.");
    }

    @Override
    public synchronized void deleteShow(int showId) {
        Show show = shows.stream().filter(item -> item.id() == showId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Show not found."));
        bookings.removeIf(booking -> booking.showId() == showId);
        deleteShows(List.of(show));
    }

    private void deleteShows(List<Show> showsToDelete) {
        List<Integer> showIds = showsToDelete.stream().map(Show::id).toList();
        if (bookings.stream().anyMatch(booking -> showIds.contains(booking.showId()))) {
            throw new IllegalStateException("Could not delete this showtime. Cancel its bookings first.");
        }
        shows.removeIf(show -> showIds.contains(show.id()));
        showIds.forEach(showId -> {
            List<Seat> removedSeats = seats.remove(showId);
            if (removedSeats != null) removedSeats.forEach(seat -> seatCategoryIds.remove(seat.id()));
        });
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
