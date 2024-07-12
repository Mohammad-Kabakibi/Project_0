package org.example.service;

import org.example.dao.UsersDAO;
import org.example.exceptions.*;
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

    public User addUser(User user) throws MyCustumException {
        if(user.getName().length() > 100 || user.getName().length() < 2 || user.getPassword().length() < 2 || user.getPassword().length() > 35)
            throw new InvalidValuesException();
        if(user.getMember_since() == null)
            user.setMember_since(Date.valueOf(LocalDateTime.now().toLocalDate().toString()));
        return usersDAO.addUser(user);
    }

    public List<User> getAllUsers() {
        return usersDAO.getAllUsers();
    }

    public User getUserById(int userId) throws MyCustumException {
        if(userId <= 0)
            throw new NigativeNumException();
        return usersDAO.getUserById(userId);
    }

    public User updateUserById(int userId, JSONObject new_user) throws MyCustumException {
        User user = getUserById(userId);
        if(new_user.has("name")) {
            String name = new_user.getString("name");
            if(name.length() > 100 || name.length() < 2)
                throw new UserInvalidNameException();
            user.setName(name);
        }
        if(new_user.has("password")){
            String password = new_user.getString("password");
            if(password.length() > 50 || password.length() < 4)
                throw new UserInvalidPasswordException();
            user.setPassword(password);
        }
        if(new_user.has("member_since"))
            user.setMember_since(Date.valueOf(new_user.getString("member_since")));

        return usersDAO.updateUserById(userId, user);
    }

    public User deleteUserById(int userId) throws MyCustumException {
        if(userId <= 0)
            throw new NigativeNumException();
        User deleted_user = usersDAO.getUserById(userId);
        usersDAO.deleteUserById(userId);
        return deleted_user;
    }

    public JSONArray getUsersByBookId(int bookId) throws MyCustumException {
        if(bookId <= 0)
            throw new NigativeNumException();
        return usersDAO.getUsersByBookId(bookId);
    }
}
