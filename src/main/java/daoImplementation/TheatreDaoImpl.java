package daoImplementation;

import configuration.DBconnection;
import dao.TheatreDao;
import model.Theatre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TheatreDaoImpl implements TheatreDao {

    @Override
    public boolean addTheatre(Theatre theatre) {
        String sql = "INSERT INTO THEATRES (THEATRE_NAME, LOCATION, CITY) VALUES (?, ?, ?)";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            setTheatreParameters(statement, theatre);
            if (statement.executeUpdate() == 0) return false;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) theatre.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Unable to add theatre: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Theatre getTheatreById(int theatreId) {
        String sql = "SELECT * FROM THEATRES WHERE THEATRE_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, theatreId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapTheatre(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch theatre: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Theatre> getAllTheatres() {
        return getTheatres("SELECT * FROM THEATRES ORDER BY THEATRE_ID", null);
    }

    @Override
    public List<Theatre> getTheatresByCity(String city) {
        return getTheatres("SELECT * FROM THEATRES WHERE CITY = ? ORDER BY THEATRE_ID", city);
    }

    @Override
    public boolean updateTheatre(Theatre theatre) {
        String sql = "UPDATE THEATRES SET THEATRE_NAME = ?, LOCATION = ?, CITY = ? "
                + "WHERE THEATRE_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setTheatreParameters(statement, theatre);
            statement.setInt(4, theatre.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to update theatre: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteTheatre(int theatreId) {
        String sql = "DELETE FROM THEATRES WHERE THEATRE_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, theatreId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to delete theatre: " + e.getMessage());
            return false;
        }
    }

    private List<Theatre> getTheatres(String sql, String city) {
        List<Theatre> theatres = new ArrayList<>();

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (city != null) statement.setString(1, city);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) theatres.add(mapTheatre(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch theatres: " + e.getMessage());
        }
        return theatres;
    }

    private void setTheatreParameters(PreparedStatement statement, Theatre theatre)
            throws SQLException {
        statement.setString(1, theatre.getName());
        statement.setString(2, theatre.getLocation());
        statement.setString(3, theatre.getCity());
    }

    private Theatre mapTheatre(ResultSet resultSet) throws SQLException {
        Theatre theatre = new Theatre();
        theatre.setId(resultSet.getInt("THEATRE_ID"));
        theatre.setName(resultSet.getString("THEATRE_NAME"));
        theatre.setLocation(resultSet.getString("LOCATION"));
        theatre.setCity(resultSet.getString("CITY"));
        return theatre;
    }
}