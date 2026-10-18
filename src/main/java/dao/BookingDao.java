package dao;

import model.Booking;

import java.util.List;

public interface BookingDao {
    boolean createBooking(Booking booking);

    Booking getBookingById(int bookingId);

    List<Booking> getAllBookings();

    List<Booking> getBookingsByClerkId(int clerkId);

    List<Booking> getBookingsByShowId(int showId);

    List<Booking> getBookingsByCustomerName(String customerName);

    boolean cancelBooking(int bookingId);
}
