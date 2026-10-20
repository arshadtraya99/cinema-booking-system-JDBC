package daoImplementation;

import configuration.DBconnection;
import dao.MovieDao;
import model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MovieDaoImpl implements MovieDao {

    @Override
    public boolean addMovie(Movie movie) {
        String sql = "INSERT INTO MOVIES (TITLE, DURATION, GENRE, POSTER, TRAILER_URL) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, movie.getTitle());
            statement.setFloat(2, movie.getDuration());
            statement.setString(3, movie.getGenre());
            if (movie.getPoster() == null) statement.setNull(4, Types.BLOB);
            else statement.setBytes(4, movie.getPoster());
            statement.setString(5, movie.getTrailerUrl());
            if (statement.executeUpdate() == 0) return false;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) movie.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Unable to add movie: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Movie getMovieById(int id) {
        String sql = "SELECT * FROM MOVIES WHERE MOVIE_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapMovie(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Unable to fetch movie: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Movie> getMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM MOVIES ORDER BY MOVIE_ID";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) movies.add(mapMovie(resultSet));
        } catch (SQLException e) {
            System.err.println("Unable to fetch movies: " + e.getMessage());
        }
        return movies;
    }

    @Override
    public boolean updateMovie(Movie movie) {
        String sql = "UPDATE MOVIES SET TITLE = ?, DURATION = ?, GENRE = ?, POSTER = ?, TRAILER_URL = ? WHERE MOVIE_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, movie.getTitle());
            statement.setFloat(2, movie.getDuration());
            statement.setString(3, movie.getGenre());
            if (movie.getPoster() == null) statement.setNull(4, Types.BLOB);
            else statement.setBytes(4, movie.getPoster());
            statement.setString(5, movie.getTrailerUrl());
            statement.setInt(6, movie.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to update movie: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMovie(int id) {
        String sql = "DELETE FROM MOVIES WHERE MOVIE_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to delete movie: " + e.getMessage());
            return false;
        }
    }

    private Movie mapMovie(ResultSet resultSet) throws SQLException {
        Movie movie = new Movie();
        movie.setId(resultSet.getInt("MOVIE_ID"));
        movie.setTitle(resultSet.getString("TITLE"));
        movie.setDuration(resultSet.getFloat("DURATION"));
        movie.setGenre(resultSet.getString("GENRE"));
        movie.setPoster(resultSet.getBytes("POSTER"));
        movie.setTrailerUrl(resultSet.getString("TRAILER_URL"));
        return movie;
    }
}