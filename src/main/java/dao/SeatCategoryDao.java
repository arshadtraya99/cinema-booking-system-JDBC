package dao;
import java.util.List;
public interface SeatCategoryDao {
    boolean addSeatCategory(SeatCategoryDao seatCategory);

    SeatCategoryDao getSeatCategoryById(int categoryId);

    List<SeatCategoryDao> getAllSeatCategories();

    boolean updateSeatCategory(SeatCategoryDao seatCategory);

    boolean deleteSeatCategory(int categoryId);
}
