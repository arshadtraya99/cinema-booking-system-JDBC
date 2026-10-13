public interface ShowDao {
    boolean addTheatre(Theatre theatre);

    Theatre getTheatreById(int theatreId);

    List<Theatre> getAllTheatres();

    List<Theatre> getTheatresByCity(String city);

    boolean updateTheatre(Theatre theatre);

    boolean deleteTheatre(int theatreId);
}
