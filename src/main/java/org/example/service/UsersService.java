package org.example.service;

import org.example.config.Result;
import org.example.dao.UsersDAO;
import org.example.model.User;
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

    public Result<User> addUser(User user) {
        if(user.getName().length() > 100 || user.getName().length() < 2 || user.getPassword().length() < 2 || user.getPassword().length() > 35)
            return new Result<>("Wrong Values", null);
        if(user.getMember_since() == null)
            user.setMember_since(Date.valueOf(LocalDateTime.now().toLocalDate().toString()));
        User added_user = usersDAO.addUser(user);
        if(added_user == null)
            return new Result<>("Something went wrong", null);
        return new Result<>("", added_user);
    }

    public List<User> getAllUsers() {
        return usersDAO.getAllUsers();
    }

    public Result<User> getUserById(int userId) {
        if(userId <= 0)
            return new Result<>("ID must be positive", null);
        User user = usersDAO.getUserById(userId);
        if(user == null)
            return new Result<>("User (ID:"+userId+") doesn't exist", null);
        return new Result<>("", user);
    }

    public Result<User> updateUserById(int userId, JSONObject new_user) {
        Result<User> user = getUserById(userId);
        if(user.getObj() != null){
            if(new_user.has("name")) {
                String name = new_user.getString("name");
                if(name.length() > 100 || name.length() < 2)
                    return new Result<>("Name must be between 2-100 characters", null);
                user.getObj().setName(name);
            }
            if(new_user.has("password")){
                String password = new_user.getString("password");
                if(password.length() > 45 || password.length() < 2)
                    return new Result<>("Password must be between 2-45 characters", null);
                user.getObj().setPassword(password);
            }
            if(new_user.has("member_since"))
                user.getObj().setMember_since(Date.valueOf(new_user.getString("member_since")));
        }
        User updated_user = usersDAO.updateUserById(userId, user.getObj());
        if(updated_user == null)
            return new Result<>("Something went wrong", null);
        return new Result<>("", updated_user);
    }

    public Result<User> deleteUserById(int userId) {
        if(userId <= 0)
            return new Result<>("ID must be positive", null);
        User deleted_user = usersDAO.getUserById(userId);
        if(usersDAO.deleteUserById(userId))
            return new Result<>("", deleted_user);
        return new Result<>("User (ID:"+userId+") doesn't exist", null);
    }

    public Result<JSONArray> getUsersByBookId(int bookId) {
        if(bookId <= 0)
            return new Result<>("ID must be positive", null);
        return new Result<>("", usersDAO.getUsersByBookId(bookId));
    }
}
