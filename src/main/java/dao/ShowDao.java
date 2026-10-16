package dao;

import model.Show;

import java.util.List;

public interface ShowDao {
    boolean addShow(Show show);

    Show getShowById(int showId);

    List<Show> getAllShows();

    List<Show> getShowsByMovieId(int movieId);

    List<Show> getShowsByScreenId(int screenId);

    boolean updateShow(Show show);

    boolean deleteShow(int showId);
}
