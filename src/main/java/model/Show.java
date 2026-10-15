package model;

public class Show {
    private int id;
    private int movieId;
    private int screenId;
    private String showTime;
    private String showDate;

    // Construtor sem parâmetros
    public Show() {
    }

    // Construtor completo
    public Show(int id, int movieId, int screenId, String showTime, String showDate) {
        this.id = id;
        this.movieId = movieId;
        this.screenId = screenId;
        this.showTime = showTime;
        this.showDate = showDate;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getScreenId() {
        return screenId;
    }

    public void setScreenId(int screenId) {
        this.screenId = screenId;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }
}