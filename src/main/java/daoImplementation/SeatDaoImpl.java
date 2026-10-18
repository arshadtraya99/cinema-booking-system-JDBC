package daoImplementation;

import configuration.DBconnection;
import dao.SeatDao;
import model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of SeatDao for the Cinema_booking_System schema. */
public class SeatDaoImpl implements SeatDao {

    @Override
    public boolean addSeat(Seat seat) {
        final String sql = "INSERT INTO Seats "
                + "(SHOW_ID, CATEGORY_ID, SEAT_ROW, `NUMBER`, ISBOOKED) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBconnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, seat.getShowId());
                statement.setInt(2, seat.getCategoryId());
                statement.setString(3, seat.getSeatRow());
                statement.setInt(4, seat.getNumber());
                statement.setBoolean(5, seat.isBooked());

                if (statement.executeUpdate() == 0) return false;

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) seat.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Unable to add seat: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Seat getSeatById(int seatId) {
        final String sql = "SELECT * FROM Seats WHERE SEAT_ID = ?";

        try (Connection connection = DBconnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, seatId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapSeat(resultSet) : null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch seat: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Seat> getAllSeats() {
        final String sql = "SELECT * FROM Seats "
                + "ORDER BY SHOW_ID, SEAT_ROW, `NUMBER`";
        List<Seat> seats = new ArrayList<>();

        try (Connection connection = DBconnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) seats.add(mapSeat(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch seats: " + e.getMessage());
        }
        return seats;
    }

    @Override
    public List<Seat> getSeatsByShowId(int showId) {
        final String sql = "SELECT * FROM Seats WHERE SHOW_ID = ? "
                + "ORDER BY SEAT_ROW, `NUMBER`";
        return getSeatsForShow(sql, showId);
    }

    @Override
    public List<Seat> getAvailableSeatsByShowId(int showId) {
        final String sql = "SELECT * FROM Seats WHERE SHOW_ID = ? "
                + "AND (ISBOOKED = FALSE OR ISBOOKED IS NULL) "
                + "ORDER BY SEAT_ROW, `NUMBER`";
        return getSeatsForShow(sql, showId);
    }

    @Override
    public boolean updateSeat(Seat seat) {
        final String sql = "UPDATE Seats SET SHOW_ID = ?, CATEGORY_ID = ?, "
                + "SEAT_ROW = ?, `NUMBER` = ?, ISBOOKED = ? WHERE SEAT_ID = ? "
                + "AND (? = TRUE OR NOT EXISTS (SELECT 1 FROM BOOKINGS "
                + "WHERE BOOKINGS.SEAT_ID = SEATS.SEAT_ID))";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seat.getShowId());
            statement.setInt(2, seat.getCategoryId());
            statement.setString(3, seat.getSeatRow());
            statement.setInt(4, seat.getNumber());
            statement.setBoolean(5, seat.isBooked());
            statement.setInt(6, seat.getId());
            statement.setBoolean(7, seat.isBooked());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to update seat: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateSeatBookingStatus(int seatId, boolean isBooked) {
        final String sql = "UPDATE Seats SET ISBOOKED = ? WHERE SEAT_ID = ? "
                + "AND (? = TRUE OR NOT EXISTS (SELECT 1 FROM BOOKINGS "
                + "WHERE BOOKINGS.SEAT_ID = SEATS.SEAT_ID))";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, isBooked);
            statement.setInt(2, seatId);
            statement.setBoolean(3, isBooked);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to update seat booking status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteSeat(int seatId) {
        final String sql = "DELETE FROM Seats WHERE SEAT_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seatId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to delete seat: " + e.getMessage());
            return false;
        }
    }

    private List<Seat> getSeatsForShow(String sql, int showId) {
        List<Seat> seats = new ArrayList<>();

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, showId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) seats.add(mapSeat(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch seats for show: " + e.getMessage());
        }
        return seats;
    }

    private Seat mapSeat(ResultSet resultSet) throws SQLException {
        Seat seat = new Seat();
        seat.setId(resultSet.getInt("SEAT_ID"));
        seat.setShowId(resultSet.getInt("SHOW_ID"));
        seat.setCategoryId(resultSet.getInt("CATEGORY_ID"));
        seat.setSeatRow(resultSet.getString("SEAT_ROW"));
        seat.setNumber(resultSet.getInt("NUMBER"));
        seat.setBooked(resultSet.getBoolean("ISBOOKED"));
        return seat;
    }
}