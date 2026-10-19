package dao;

import model.SeatCategory;

import java.util.List;

public interface SeatCategoryDao {
    boolean addSeatCategory(SeatCategory seatCategory);

    SeatCategory getSeatCategoryById(int categoryId);

    List<SeatCategory> getAllSeatCategories();

    boolean updateSeatCategory(SeatCategory seatCategory);

    boolean deleteSeatCategory(int categoryId);
}
