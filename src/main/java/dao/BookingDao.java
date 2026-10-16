package dao;
import java.util.List;
public interface BookingDao {
     boolean createBooking(BookingDao booking);

    BookingDao getBookingById(int bookingId);

    List<BookingDao> getAllBookings();

    List<BookingDao> getBookingsByClerkId(int clerkId);

    List<BookingDao> getBookingsByShowId(int showId);

    List<BookingDao> getBookingsByCustomerName(String customerName);

    boolean cancelBooking(int bookingId);
}
