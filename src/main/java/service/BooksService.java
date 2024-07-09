package service;

import dao.BooksDAO;
import model.Book;
import model.User;
import org.json.JSONObject;

import java.sql.Date;
import java.util.List;

public class BooksService {
    private BooksDAO booksDAO;

    public BooksService(){
        this.booksDAO = new BooksDAO();
    }

    public Book addBook(Book book) {
        if(book.getPrice() < 0)
            return null;
        return booksDAO.addBook(book);
    }

    public List<Book> getAllBooks() {
        return booksDAO.getAllBooks();
    }

    public Book getBookById(int bookId) {
        if(bookId <= 0)
            return null;
        return booksDAO.getBookById(bookId);
    }

    public Book updateBookById(int bookId, JSONObject new_book) {
        Book book = getBookById(bookId);
        if(book != null){
            if(new_book.has("title"))
                book.setTitle(new_book.getString("title"));
            if(new_book.has("author_name"))
                book.setAuthor_name(new_book.getString("author_name"));
            if(new_book.has("category"))
                book.setCategory(new_book.getString("category"));
            if(new_book.has("summary"))
                book.setSummary(new_book.getString("summary"));
            if(new_book.has("year"))
                book.setYear(Date.valueOf(new_book.getString("year")));
            if(new_book.has("price"))
                book.setPrice(new_book.getDouble("price"));
            // Cover Img =================================================================================
        }

        return booksDAO.updateBookById(bookId, book);
    }

    public Book deleteBookById(int bookId) {
        if(bookId <= 0)
            return null;
        Book deleted_book = booksDAO.getBookById(bookId);
        if(booksDAO.deleteBookById(bookId))
            return deleted_book;
        return null;
    }
}
