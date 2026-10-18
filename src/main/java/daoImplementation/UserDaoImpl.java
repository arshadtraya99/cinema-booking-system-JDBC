package daoImplementation;

import configuration.DBconnection;
import dao.UserDao;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    @Override
    public User login(String username, String password) {
        String sql = "SELECT * FROM USERS WHERE USERNAME = ? AND PASSWORD = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Unable to authenticate user: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean addUser(User user) {

        String sql = "INSERT INTO USERS (NAME, USERNAME, PASSWORD, ROLE) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole());

            if (statement.executeUpdate() == 0) return false;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Unable to add user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User getUserById(int userId) {

        String sql = "SELECT * FROM USERS WHERE USER_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }

        } catch (SQLException e) {
            System.err.println("Unable to fetch user: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM USERS ORDER BY USER_ID";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                users.add(mapUser(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Unable to fetch users: " + e.getMessage());
        }

        return users;
    }

    @Override
    public boolean updateUser(User user) {

        String sql = """
                UPDATE USERS
                SET NAME = ?, USERNAME = ?, PASSWORD = ?, ROLE = ?
                WHERE USER_ID = ?
                """;

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole());
            statement.setInt(5, user.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Unable to update user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteUser(int userId) {

        String sql = "DELETE FROM USERS WHERE USER_ID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Unable to delete user: " + e.getMessage());
            return false;
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("USER_ID"));
        user.setName(resultSet.getString("NAME"));
        user.setUsername(resultSet.getString("USERNAME"));
        user.setPassword(resultSet.getString("PASSWORD"));
        user.setRole(resultSet.getString("ROLE"));
        return user;
    }
}