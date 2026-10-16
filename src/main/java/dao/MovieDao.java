
package dao;
import java.util.List;
public interface MovieDao {
    boolean addMovie(MovieDao movie);

    MovieDao getMovieById(int id);

    List<MovieDao> getMovies();

    boolean updateMovie(MovieDao movie);

    boolean deleteMovie(int id);
}