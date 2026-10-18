package dao;

import model.Seat;

import java.util.List;

public interface SeatDao {
    boolean addSeat(Seat seat);

    Seat getSeatById(int seatId);

    List<Seat> getAllSeats();

    List<Seat> getSeatsByShowId(int showId);

    List<Seat> getAvailableSeatsByShowId(int showId);

    boolean updateSeat(Seat seat);

    boolean updateSeatBookingStatus(int seatId, boolean isBooked);

    boolean deleteSeat(int seatId);
}
