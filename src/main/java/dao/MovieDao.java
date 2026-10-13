public interface MovieDao {
    boolean addMovie(Movie movie);

    Movie getMovieById(int id);

    List<Movies> getMovies();

    boolean updateMovie(Movie movie);

    boolean deleteMovie(int id);
}