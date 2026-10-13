import java.sql.Connection;
import java.sql.DriverManager;

public class DBconnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/Cinema_booking_System";

    private static final String USER = "root";
    private static final String PASSWORD = "your_password";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
