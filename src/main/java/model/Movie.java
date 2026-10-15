package model;

public class Movie {
    private int id;
    private String title;
    private float duration;
    private String genre;

    // Construtor sem parâmetros
    public Movie() {
    }

    // Construtor completo
    public Movie(int id, String title, float duration, String genre) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.genre = genre;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}