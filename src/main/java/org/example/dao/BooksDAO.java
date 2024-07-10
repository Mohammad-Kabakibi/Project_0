package org.example.dao;

import org.example.model.Book;
import org.json.JSONArray;
import org.json.JSONObject;
import org.example.util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BooksDAO {

    private Connection connection;

    public BooksDAO(){
        this.connection = ConnectionUtil.getConnection();
    }

    public Book addBook(Book book) {
        try {
            String sql = "insert into books(title, author_name, category, year, price, cover_img, summary)" +
                    " values(?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor_name());
            preparedStatement.setString(3, book.getCategory());
            preparedStatement.setDate(4, book.getYear());
            preparedStatement.setDouble(5, book.getPrice());
            preparedStatement.setString(6, book.getCover_img());
            preparedStatement.setString(7, book.getSummary());

            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getGeneratedKeys(); // to get the id of the inserted book
            if(resultSet.next()){
                int book_id = resultSet.getInt(1);
                book.setId(book_id);
                return book;
            }
        }catch(
                SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Book getBookById(int book_id){
        try {
            String sql = "select * from books where id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, book_id);

            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                Book book = new Book(rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author_name"),
                        rs.getString("category"),
                        rs.getDate("year"),
                        rs.getDouble("price"),
                        rs.getString("cover_img"),
                        rs.getString("summary"));
                return book;
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Book> getAllBooks(){
        try {
            String sql = "select * from books;";
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            ArrayList<Book> books = new ArrayList<>();

            while(rs.next()){
                books.add(new Book(rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author_name"),
                        rs.getString("category"),
                        rs.getDate("year"),
                        rs.getDouble("price"),
                        rs.getString("cover_img"),
                        rs.getString("summary")));
            }
            return books;
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean deleteBookById(int book_id){
        try {
            String sql = "delete from books where id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, book_id);

            int deleted_rows = preparedStatement.executeUpdate();
            return deleted_rows != 0; // should be 1
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public Book updateBookById(int book_id, Book book){
        try {
            String sql = "update books set title = ?, author_name = ?, category = ?, year = ?, price = ?" +
                    ", cover_img = ?, summary = ? where id = ? ;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor_name());
            preparedStatement.setString(3, book.getCategory());
            preparedStatement.setDate(4, book.getYear());
            preparedStatement.setDouble(5, book.getPrice());
            preparedStatement.setString(6, book.getCover_img());
            preparedStatement.setString(7, book.getSummary());
            preparedStatement.setInt(8, book_id);

            preparedStatement.execute();
            return book;
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public JSONArray getBooksByUserId(int userId) {
        JSONArray books = new JSONArray();
        try{
            String sql = "select books.id, title, author_name, category, year, price, cover_img, summary, copies " +
                    "from books inner join bought_books on books.id = bought_books.book_id " +
                    "inner join users ON users.id = bought_books.user_id " +
                    "where users.id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()){
                JSONObject jo = new JSONObject();
                jo.put("id", rs.getInt("id"));
                jo.put("title", rs.getString("title"));
                jo.put("author_name", rs.getString("author_name"));
                jo.put("category", rs.getString("category"));
                jo.put("year", rs.getDate("year"));
                jo.put("price", rs.getDouble("price"));
                jo.put("cover_img", rs.getString("cover_img"));
                jo.put("summary", rs.getString("summary"));
                jo.put("copies", rs.getInt("copies"));
                books.put(jo);
            }
        }catch(Exception q){}
        return books;
    }

    public JSONArray getMostKBooks(int k) {
        JSONArray books = new JSONArray();
        try{
            String sql = "select books.id, title, author_name, category, year, price, cover_img, summary, sum(bought_books.copies) as copies " +
                    "from books inner join bought_books on books.id = bought_books.book_id " +
                    "group by books.id order by copies DESC limit ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, k);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()){
                JSONObject jo = new JSONObject();
                jo.put("id", rs.getInt("id"));
                jo.put("title", rs.getString("title"));
                jo.put("author_name", rs.getString("author_name"));
                jo.put("category", rs.getString("category"));
                jo.put("year", rs.getDate("year"));
                jo.put("price", rs.getDouble("price"));
                jo.put("cover_img", rs.getString("cover_img"));
                jo.put("summary", rs.getString("summary"));
                jo.put("copies_sold", rs.getInt("copies"));
                books.put(jo);
            }
        }catch(Exception q){}
        return books;
    }

    public boolean existingBook(String title) {
        try {
            String sql = "select * from books where title = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, title);

            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return false;
    }
}
