package daoImplementation;

import configuration.DBconnection;
import dao.BookingDao;
import model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookingDaoImpl implements BookingDao {

    @Override
    public boolean createBooking(Booking booking) {
        String lockSeatSql = "SELECT SHOW_ID, ISBOOKED FROM SEATS WHERE SEAT_ID = ? FOR UPDATE";
        String insertSql = "INSERT INTO BOOKINGS "
                + "(SHOW_ID, SEAT_ID, CLERK_ID, CUSTOMER_NAME, TOTAL_AMOUNT) "
                + "VALUES (?, ?, ?, ?, ?)";
        String updateSeatSql = "UPDATE SEATS SET ISBOOKED = TRUE "
                + "WHERE SEAT_ID = ? AND (ISBOOKED = FALSE OR ISBOOKED IS NULL)";

        try (Connection connection = DBconnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(lockSeatSql)) {
                    statement.setInt(1, booking.getSeatId());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()
                                || resultSet.getInt("SHOW_ID") != booking.getShowId()
                                || resultSet.getBoolean("ISBOOKED")) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, booking.getShowId());
                    statement.setInt(2, booking.getSeatId());
                    statement.setInt(3, booking.getClerkId());
                    statement.setString(4, booking.getCustomerName());
                    statement.setDouble(5, booking.getTotalAmount());
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) booking.setId(keys.getInt(1));
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(updateSeatSql)) {
                    statement.setInt(1, booking.getSeatId());
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Unable to create booking: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM BOOKINGS WHERE BOOKING_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapBooking(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch booking: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Booking> getAllBookings() {
        return getBookings("SELECT * FROM BOOKINGS ORDER BY BOOKING_TIME DESC", null, null);
    }

    @Override
    public List<Booking> getBookingsByClerkId(int clerkId) {
        return getBookings("SELECT * FROM BOOKINGS WHERE CLERK_ID = ? "
                + "ORDER BY BOOKING_TIME DESC", clerkId, null);
    }

    @Override
    public List<Booking> getBookingsByShowId(int showId) {
        return getBookings("SELECT * FROM BOOKINGS WHERE SHOW_ID = ? "
                + "ORDER BY BOOKING_TIME DESC", showId, null);
    }

    @Override
    public List<Booking> getBookingsByCustomerName(String customerName) {
        return getBookings("SELECT * FROM BOOKINGS WHERE CUSTOMER_NAME = ? "
                + "ORDER BY BOOKING_TIME DESC", null, customerName);
    }

    @Override
    public boolean cancelBooking(int bookingId) {
        String findSql = "SELECT SEAT_ID FROM BOOKINGS WHERE BOOKING_ID = ? FOR UPDATE";
        String deleteSql = "DELETE FROM BOOKINGS WHERE BOOKING_ID = ?";
        String updateSeatSql = "UPDATE SEATS SET ISBOOKED = FALSE WHERE SEAT_ID = ?";

        try (Connection connection = DBconnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int seatId;
                try (PreparedStatement statement = connection.prepareStatement(findSql)) {
                    statement.setInt(1, bookingId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        seatId = resultSet.getInt("SEAT_ID");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
                    statement.setInt(1, bookingId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(updateSeatSql)) {
                    statement.setInt(1, seatId);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Unable to cancel booking: " + e.getMessage());
            return false;
        }
    }

    private List<Booking> getBookings(String sql, Integer id, String customerName) {
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (id != null) {
                statement.setInt(1, id);
            } else if (customerName != null) {
                statement.setString(1, customerName);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) bookings.add(mapBooking(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch bookings: " + e.getMessage());
        }
        return bookings;
    }

    private Booking mapBooking(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking();
        booking.setId(resultSet.getInt("BOOKING_ID"));
        booking.setShowId(resultSet.getInt("SHOW_ID"));
        booking.setSeatId(resultSet.getInt("SEAT_ID"));
        booking.setClerkId(resultSet.getInt("CLERK_ID"));
        booking.setCustomerName(resultSet.getString("CUSTOMER_NAME"));
        booking.setTotalAmount(resultSet.getDouble("TOTAL_AMOUNT"));
        booking.setBookingTime(resultSet.getString("BOOKING_TIME"));
        return booking;
    }
}