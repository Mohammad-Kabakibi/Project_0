package service;

import dao.UsersDAO;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
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
            if(new_user.has("name")) {
                String name = new_user.getString("name");
                if(name.length() > 100 || name.length() < 2)
                    return null;
                user.setName(name);
            }
            if(new_user.has("password")){
                String password = new_user.getString("password");
                if(password.length() > 35 || password.length() < 2)
                    return null;
                user.setPassword(password);
            }
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

    public JSONArray getUsersByBookId(int bookId) {
        if(bookId <= 0)
            return new JSONArray();
        return usersDAO.getUsersByBookId(bookId);
    }
}
