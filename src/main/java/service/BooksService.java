package service;

import config.Result;
import dao.BooksDAO;
import model.Book;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.Date;
import java.util.List;

public class BooksService {
    private BooksDAO booksDAO;

    public BooksService(){
        this.booksDAO = new BooksDAO();
    }

    public Result<Book> addBook(Book book) {
        if(book.getPrice() < 0 || book.getTitle().length() < 2 || booksDAO.existingBook(book.getTitle()))
            return new Result<>("Wrong Values", null);
        return new Result<>("", booksDAO.addBook(book));
    }

    public List<Book> getAllBooks() {
        return booksDAO.getAllBooks();
    }

    public Result<Book> getBookById(int bookId) {
        if(bookId <= 0)
            return new Result<>("ID must be positive number.", null);
        Book book = booksDAO.getBookById(bookId);
        if(book == null)
            return new Result<>("Book (ID:"+bookId+") doesn't exist", null);
        return new Result<>("", book);
    }

    public Result<Book> updateBookById(int bookId, JSONObject new_book) {
        Result<Book> book = getBookById(bookId);
        String old_img = null;
        // Patch request...
        Book updated_book = null;
        if(book.getObj() != null){
            old_img = book.getObj().getTitle()+"."+(book.getObj().getCover_img().split("\\."))[1];
            if(new_book.has("title"))
                book.getObj().setTitle(new_book.getString("title"));
            if(new_book.has("author_name"))
                book.getObj().setAuthor_name(new_book.getString("author_name"));
            if(new_book.has("category"))
                book.getObj().setCategory(new_book.getString("category"));
            if(new_book.has("summary"))
                book.getObj().setSummary(new_book.getString("summary"));
            if(new_book.has("year"))
                book.getObj().setYear(Date.valueOf(new_book.getString("year")));
            if(new_book.has("price"))
                book.getObj().setPrice(new_book.getDouble("price"));
            if(new_book.has("cover_img"))
                book.getObj().setCover_img(new_book.getString("cover_img"));
            updated_book = booksDAO.updateBookById(bookId, book.getObj());
        }
        else{
            return new Result<>("Book (ID:"+bookId+") doesn't exist", null);
        }
        if(updated_book == null)
            return new Result<>("Something went wrong", null);
        var updated_book_result = new Result<Book>("", updated_book);
        if(new_book.has("cover_img")){ // deleting the old image
            try {
                if(isDefaultImage(old_img))
                    return updated_book_result;
                var cover_img = new File("upload/images/" + old_img);
                if (cover_img.exists())
                    cover_img.delete();
            }catch (Exception q){
                System.out.println(q.getMessage());
            }
        }
        return updated_book_result;
    }

    public Result<Book> deleteBookById(int bookId) {
        if(bookId <= 0)
            return new Result<>("ID must be positive", null);
        Book deleted_book = booksDAO.getBookById(bookId);
        if(deleted_book == null)
            return new Result<>("Book (ID:"+bookId+") doesn't exist", null);
        Result<Book> deleted_book_result = new Result<>("", deleted_book);
        if(booksDAO.deleteBookById(bookId)){
            try {
                if(isDefaultImage(deleted_book.getCover_img()))
                    return deleted_book_result;
                String ext = "."+(deleted_book.getCover_img().split("\\."))[1];
                var cover_img = new File("upload/images/" + deleted_book.getTitle() + ext);
                if (cover_img.exists())
                    cover_img.delete();
            }catch (Exception q){
                System.out.println(q.getMessage());
            }
            return deleted_book_result;
        }
        return new Result<>("Something went wrong", null);
    }

    public Result<JSONArray> getBooksByUserId(int userId) {
        if(userId <= 0)
            return new Result<>("ID must be positive", null);
        JSONArray book = booksDAO.getBooksByUserId(userId);
        if(book == null)
            return new Result<>("Something went wrong", null);
        return new Result<>("", book);
    }

    public Result<JSONArray> getMostKBooks(int k) {
        if(k <= 0)
            return new Result<>("The number must be positive", null);
        return new Result<>("", booksDAO.getMostKBooks(k));
    }

    private boolean isDefaultImage(String cover_img){
        return cover_img.endsWith("default_cover_img.jpg");
    }
}
