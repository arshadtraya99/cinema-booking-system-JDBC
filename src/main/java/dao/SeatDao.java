import java.util.List;

public interface SeatDao {
    boolean addSeat(SeatDao seat);

    SeatDao getSeatById(int seatId);

    List<SeatDao> getAllSeats();

    List<SeatDao> getSeatsByShowId(int showId);

    List<SeatDao> getAvailableSeatsByShowId(int showId);

    boolean updateSeat(SeatDao seat);

    boolean updateSeatBookingStatus(int seatId, boolean isBooked);

    boolean deleteSeat(int seatId);
}
