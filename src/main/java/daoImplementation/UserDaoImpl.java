package main.java.daoImplementation;

import configuration.DBconnection;
import dao.UserDao;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    @Override
    public boolean addUser(User user) {

        String sql = "INSERT INTO Users (Name, Username, Password, Role) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User getUserById(int userId) {

        String sql = "SELECT * FROM Users WHERE UserID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                User user = new User();

                user.setUserId(resultSet.getInt("UserID"));
                user.setName(resultSet.getString("Name"));
                user.setUsername(resultSet.getString("Username"));
                user.setPassword(resultSet.getString("Password"));
                user.setRole(resultSet.getString("Role"));

                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM Users";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                User user = new User();

                user.setUserId(resultSet.getInt("UserID"));
                user.setName(resultSet.getString("Name"));
                user.setUsername(resultSet.getString("Username"));
                user.setPassword(resultSet.getString("Password"));
                user.setRole(resultSet.getString("Role"));

                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public boolean updateUser(User user) {

        String sql = """
                UPDATE Users
                SET Name = ?, Username = ?, Password = ?, Role = ?
                WHERE UserID = ?
                """;

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole());
            statement.setInt(5, user.getUserId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteUser(int userId) {

        String sql = "DELETE FROM Users WHERE UserID = ?";

        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}