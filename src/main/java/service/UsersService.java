package service;

import dao.UsersDAO;
import model.User;
import org.json.JSONObject;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class UsersService {
    private UsersDAO usersDAO;

    public UsersService(){
        this.usersDAO = new UsersDAO();
    }

    public User addUser(User user) {
        if(user.getName().length() > 100 || user.getName().length() < 2 || user.getPassword().length() < 2 || user.getPassword().length() > 35)
            return null;
        if(user.getMember_since() == null)
            user.setMember_since(Date.valueOf(LocalDateTime.now().toLocalDate().toString()));
        return usersDAO.addUser(user);
    }

    public List<User> getAllUsers() {
        return usersDAO.getAllUsers();
    }

    public User getUserById(int userId) {
        if(userId <= 0)
            return null;
        return usersDAO.getUserById(userId);
    }

    public User updateUserById(int userId, JSONObject new_user) {
        User user = getUserById(userId);
        if(user != null){
            if(new_user.has("name"))
                user.setName(new_user.getString("name"));
            if(new_user.has("password"))
                user.setPassword(new_user.getString("password"));
            if(new_user.has("member_since"))
                user.setMember_since(Date.valueOf(new_user.getString("member_since")));
        }
        return usersDAO.updateUserById(userId, user);
    }

    public User deleteUserById(int userId) {
        if(userId <= 0)
            return null;
        User deleted_user = usersDAO.getUserById(userId);
        if(usersDAO.deleteUserById(userId))
            return deleted_user;
        return null;
    }
}
