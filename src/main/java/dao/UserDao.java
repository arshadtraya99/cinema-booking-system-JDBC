package dao;
import java.util.List;
public interface UserDao {
    UserDao login(String username, String password);

    UserDao getUserById(int userId);

    List<UserDao> getAllUsers();

    boolean addUser(UserDao user);

    boolean updateUser(UserDao user);

    boolean deleteUser(int userId);
}
