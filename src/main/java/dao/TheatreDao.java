package dao;

import model.Theatre;

import java.util.List;

public interface TheatreDao {
    boolean addTheatre(Theatre theatre);

    Theatre getTheatreById(int theatreId);

    List<Theatre> getAllTheatres();

    List<Theatre> getTheatresByCity(String city);

    boolean updateTheatre(Theatre theatre);

    boolean deleteTheatre(int theatreId);
}
