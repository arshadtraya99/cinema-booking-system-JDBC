package model;

public class SeatCategory {
    private int id;
    private String categoryName;
    private double price;

    // Construtor sem parâmetros
    public SeatCategory() {
    }

    // Construtor completo
    public SeatCategory(int id, String categoryName, double price) {
        this.id = id;
        this.categoryName = categoryName;
        this.price = price;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}