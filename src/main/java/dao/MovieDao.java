
package dao;

import model.Movie;

import java.util.List;

public interface MovieDao {
    boolean addMovie(Movie movie);

    Movie getMovieById(int id);

    List<Movie> getMovies();

    boolean updateMovie(Movie movie);

    boolean deleteMovie(int id);
}