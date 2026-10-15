package model;

public class Seat {
    private int id;
    private int showId;
    private int categoryId;
    private String seatRow;
    private int number;
    private boolean isBooked;

    // Construtor sem parâmetros
    public Seat() {
    }

    // Construtor completo
    public Seat(int id, int showId, int categoryId, String seatRow, int number, boolean isBooked) {
        this.id = id;
        this.showId = showId;
        this.categoryId = categoryId;
        this.seatRow = seatRow;
        this.number = number;
        this.isBooked = isBooked;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean isBooked) {
        this.isBooked = isBooked;
    }
}