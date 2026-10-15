package model;

public class Booking {
    private int id;
    private int showId;
    private int seatId;
    private int clerkId;
    private String customerName;
    private double totalAmount;
    private String bookingTime;

    // Construtor sem parâmetros
    public Booking() {
    }

    // Construtor completo
    public Booking(int id, int showId, int seatId, int clerkId, String customerName, double totalAmount, String bookingTime) {
        this.id = id;
        this.showId = showId;
        this.seatId = seatId;
        this.clerkId = clerkId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.bookingTime = bookingTime;
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

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getClerkId() {
        return clerkId;
    }

    public void setClerkId(int clerkId) {
        this.clerkId = clerkId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }
}