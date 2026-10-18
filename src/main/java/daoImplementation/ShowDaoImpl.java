package daoImplementation;

import configuration.DBconnection;
import dao.ShowDao;
import model.Show;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ShowDaoImpl implements ShowDao {

    @Override
    public boolean addShow(Show show) {
        String sql = "INSERT INTO SHOWS (MOVIE_ID, SCREEN_ID, SHOW_TIME, SHOW_DATE) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            setShowParameters(statement, show);
            if (statement.executeUpdate() == 0) return false;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) show.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Unable to add show: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Show getShowById(int showId) {
        String sql = "SELECT * FROM SHOWS WHERE SHOW_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, showId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapShow(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch show: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Show> getAllShows() {
        return getShows("SELECT * FROM SHOWS ORDER BY SHOW_DATE, SHOW_TIME", null);
    }

    @Override
    public List<Show> getShowsByMovieId(int movieId) {
        return getShows("SELECT * FROM SHOWS WHERE MOVIE_ID = ? "
                + "ORDER BY SHOW_DATE, SHOW_TIME", movieId);
    }

    @Override
    public List<Show> getShowsByScreenId(int screenId) {
        return getShows("SELECT * FROM SHOWS WHERE SCREEN_ID = ? "
                + "ORDER BY SHOW_DATE, SHOW_TIME", screenId);
    }

    @Override
    public boolean updateShow(Show show) {
        String sql = "UPDATE SHOWS SET MOVIE_ID = ?, SCREEN_ID = ?, SHOW_TIME = ?, "
                + "SHOW_DATE = ? WHERE SHOW_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setShowParameters(statement, show);
            statement.setInt(5, show.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to update show: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteShow(int showId) {
        String sql = "DELETE FROM SHOWS WHERE SHOW_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, showId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to delete show: " + e.getMessage());
            return false;
        }
    }

    private List<Show> getShows(String sql, Integer parameter) {
        List<Show> shows = new ArrayList<>();

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) statement.setInt(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) shows.add(mapShow(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch shows: " + e.getMessage());
        }
        return shows;
    }

    private void setShowParameters(PreparedStatement statement, Show show) throws SQLException {
        statement.setInt(1, show.getMovieId());
        statement.setInt(2, show.getScreenId());
        statement.setString(3, show.getShowTime());
        statement.setString(4, show.getShowDate());
    }

    private Show mapShow(ResultSet resultSet) throws SQLException {
        Show show = new Show();
        show.setId(resultSet.getInt("SHOW_ID"));
        show.setMovieId(resultSet.getInt("MOVIE_ID"));
        show.setScreenId(resultSet.getInt("SCREEN_ID"));
        show.setShowTime(resultSet.getString("SHOW_TIME"));
        show.setShowDate(resultSet.getString("SHOW_DATE"));
        return show;
    }
}