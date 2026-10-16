package dao;
import java.util.List;

public interface TheatreDao {
    boolean addTheatre(TheatreDao theatre);

    TheatreDao getTheatreById(int theatreId);

    List<TheatreDao > getAllTheatres();

    List<TheatreDao> getTheatresByCity(String city);

    boolean updateTheatre(TheatreDao theatre);

    boolean deleteTheatre(int theatreId);
}
