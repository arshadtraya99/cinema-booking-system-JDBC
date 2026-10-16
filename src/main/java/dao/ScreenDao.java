package dao;

import java.util.List;

public interface ScreenDao {
    boolean addScreen(ScreenDao screen);

    ScreenDao getScreenById(int screenId);

    List<ScreenDao> getAllScreens();

    List<ScreenDao> getScreensByTheatreId(int theatreId);

    boolean updateScreen(ScreenDao screen);

    boolean deleteScreen(int screenId);
}
