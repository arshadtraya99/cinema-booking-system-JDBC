package dao;

import model.Screen;

import java.util.List;

public interface ScreenDao {
    boolean addScreen(Screen screen);

    Screen getScreenById(int screenId);

    List<Screen> getAllScreens();

    List<Screen> getScreensByTheatreId(int theatreId);

    boolean updateScreen(Screen screen);

    boolean deleteScreen(int screenId);
}
