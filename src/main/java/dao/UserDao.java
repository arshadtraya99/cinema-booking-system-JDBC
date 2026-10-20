package dao;

import model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    User login(String username, String password) throws SQLException;

    User getUserById(int userId);

    List<User> getAllUsers();

    boolean addUser(User user) throws SQLException;

    boolean updateUser(User user);

    boolean deleteUser(int userId);
}
