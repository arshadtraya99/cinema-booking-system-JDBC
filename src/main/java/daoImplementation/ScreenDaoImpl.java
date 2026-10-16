package daoImplementation;

import configuration.DBconnection;
import dao.ScreenDao;
import model.Screen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScreenDaoImpl implements ScreenDao {

    @Override
    public boolean addScreen(Screen screen) {

        String sql = "INSERT INTO Screens (THEATRE_ID, SCREEN_NAME, TOTAL_SEATS) VALUES (?, ?, ?)";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, screen.getTheatreId());
            statement.setString(2, screen.getName());
            statement.setInt(3, screen.getTotalSeats());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Screen getScreenById(int screenId) {

        String sql = "SELECT * FROM Screens WHERE screen_id = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, screenId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Screen screen = new Screen();

                    screen.setId(resultSet.getInt("screen_id"));
                    screen.setTheatreId(resultSet.getInt("THEATRE_ID"));
                    screen.setName(resultSet.getString("SCREEN_NAME"));
                    screen.setTotalSeats(resultSet.getInt("TOTAL_SEATS"));

                    return screen;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Screen> getAllScreens() {

        List<Screen> screens = new ArrayList<>();

        String sql = "SELECT * FROM Screens";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Screen screen = new Screen();

                screen.setId(resultSet.getInt("screen_id"));
                screen.setTheatreId(resultSet.getInt("THEATRE_ID"));
                screen.setName(resultSet.getString("SCREEN_NAME"));
                screen.setTotalSeats(resultSet.getInt("TOTAL_SEATS"));

                screens.add(screen);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return screens;
    }

    @Override
    public List<Screen> getScreensByTheatreId(int theatreId) {

        List<Screen> screens = new ArrayList<>();

        String sql = "SELECT * FROM Screens WHERE THEATRE_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, theatreId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Screen screen = new Screen();

                    screen.setId(resultSet.getInt("screen_id"));
                    screen.setTheatreId(resultSet.getInt("THEATRE_ID"));
                    screen.setName(resultSet.getString("SCREEN_NAME"));
                    screen.setTotalSeats(resultSet.getInt("TOTAL_SEATS"));

                    screens.add(screen);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return screens;
    }

    @Override
    public boolean updateScreen(Screen screen) {

        String sql = """
                UPDATE Screens
                SET THEATRE_ID = ?, SCREEN_NAME = ?, TOTAL_SEATS = ?
                WHERE screen_id = ?
                """;

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, screen.getTheatreId());
            statement.setString(2, screen.getName());
            statement.setInt(3, screen.getTotalSeats());
            statement.setInt(4, screen.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteScreen(int screenId) {

        String sql = "DELETE FROM Screens WHERE screen_id = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, screenId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}