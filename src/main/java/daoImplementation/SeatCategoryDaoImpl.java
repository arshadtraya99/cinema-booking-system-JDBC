package daoImplementation;

import configuration.DBconnection;
import dao.SeatCategoryDao;
import model.SeatCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SeatCategoryDaoImpl implements SeatCategoryDao {

    @Override
    public boolean addSeatCategory(SeatCategory seatCategory) {
        String sql = "INSERT INTO SEAT_CATEGORY (CATEGORY_NAME, PRICE) VALUES (?, ?)";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, seatCategory.getCategoryName());
            statement.setDouble(2, seatCategory.getPrice());
            if (statement.executeUpdate() == 0) return false;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) seatCategory.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Unable to add seat category: " + e.getMessage());
            return false;
        }
    }

    @Override
    public SeatCategory getSeatCategoryById(int categoryId) {
        String sql = "SELECT * FROM SEAT_CATEGORY WHERE CATEGORY_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapSeatCategory(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch seat category: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<SeatCategory> getAllSeatCategories() {
        List<SeatCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM SEAT_CATEGORY ORDER BY CATEGORY_ID";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) categories.add(mapSeatCategory(resultSet));
        } catch (SQLException e) {
            System.err.println("Unable to fetch seat categories: " + e.getMessage());
        }
        return categories;
    }

    @Override
    public boolean updateSeatCategory(SeatCategory seatCategory) {
        String sql = "UPDATE SEAT_CATEGORY SET CATEGORY_NAME = ?, PRICE = ? "
                + "WHERE CATEGORY_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seatCategory.getCategoryName());
            statement.setDouble(2, seatCategory.getPrice());
            statement.setInt(3, seatCategory.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to update seat category: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteSeatCategory(int categoryId) {
        String sql = "DELETE FROM SEAT_CATEGORY WHERE CATEGORY_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to delete seat category: " + e.getMessage());
            return false;
        }
    }

    private SeatCategory mapSeatCategory(ResultSet resultSet) throws SQLException {
        SeatCategory category = new SeatCategory();
        category.setId(resultSet.getInt("CATEGORY_ID"));
        category.setCategoryName(resultSet.getString("CATEGORY_NAME"));
        category.setPrice(resultSet.getDouble("PRICE"));
        return category;
    }
}