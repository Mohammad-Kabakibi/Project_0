package dao;

import model.User;
import util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersDAO {

    public User addUser(User user) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "insert into users(name, password, member_since) values(?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setDate(3, user.getMember_since());

            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getGeneratedKeys(); // to get the id of the inserted user
            if(resultSet.next()){
                int user_id = resultSet.getInt(1);
                user.setId(user_id);
                return user;
            }
        }catch(
                SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public User getUserById(int user_id){
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "select * from users where id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, user_id);

            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                User user = new User(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getDate("member_since"));
                return user;
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers(){
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "select * from users;";
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            ArrayList<User> users = new ArrayList<>();

            while(rs.next()){
                users.add(new User(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getDate("member_since")));
            }
            return users;
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean deleteUserById(int user_id){
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "delete from users where id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, user_id);

            int deleted_rows = preparedStatement.executeUpdate();
            return deleted_rows != 0; // should be 1
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public User updateUserById(int user_id, User user){
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "update users set name = ?, password = ?, member_since = ? where id = ? ;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setDate(3, user.getMember_since());
            preparedStatement.setInt(4, user_id);

            preparedStatement.execute();
            return user;
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}
