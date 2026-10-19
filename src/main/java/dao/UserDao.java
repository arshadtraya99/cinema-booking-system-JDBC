package dao;

import model.User;

import java.util.List;

public interface UserDao {
    User login(String username, String password);

    User getUserById(int userId);

    List<User> getAllUsers();

    boolean addUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(int userId);
}
