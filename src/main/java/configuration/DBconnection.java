package configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {

    private static final String URL = System.getenv().getOrDefault(
            "CINEMA_DB_URL", "jdbc:mysql://localhost:3306/Cinema_booking_System");
    private static final String USER = System.getenv().getOrDefault("CINEMA_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("CINEMA_DB_PASSWORD", "Shubham145");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
