package org.example.service;

import io.javalin.http.UploadedFile;
import org.apache.commons.io.FileUtils;
import org.example.exceptions.*;
import org.example.dao.BooksDAO;
import org.example.model.Book;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.Date;
import java.util.List;

import static org.example.util.StaticFilesUtil.*;

public class BooksService {
    private BooksDAO booksDAO;

    public BooksService(){
        this.booksDAO = new BooksDAO();
    }

    public Book addBook(Book book) throws MyCustumException {
        if(book.getPrice() < 0 || book.getTitle().length() < 2 )
            throw new InvalidValuesException();
        if(booksDAO.existingBook(book.getTitle()))
            throw new BookTitleExistsException();
        return booksDAO.addBook(book);
    }

    public List<Book> getAllBooks() {
        return booksDAO.getAllBooks();
    }

    public Book getBookById(int bookId) throws MyCustumException{
        if(bookId <= 0)
            throw new NigativeNumException();
        return booksDAO.getBookById(bookId);
    }

    public Book updateBookById(int bookId, JSONObject new_book, UploadedFile new_img) throws MyCustumException{
        Book book = getBookById(bookId);
        String file_name = null;
        // Patch request...
        var arr = book.getCover_img().split("/");
        String old_img = arr[arr.length-1];
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
        if(new_book.has("cover_img")) {
            file_name = book.getTitle().replaceAll("[^a-zA-Z0-9]","_") + new_img.extension();
            book.setCover_img(new_book.getString("cover_img")+IMAGES_FOLDER+file_name);
        }
        Book updated_book = booksDAO.updateBookById(bookId, book);

        if(new_book.has("cover_img")){ // deleting the old image
            try {
                if(!isDefaultImage(old_img)) {
                    var cover_img = new File(UPLOAD_FOLDER + IMAGES_FOLDER + old_img);
                    if (cover_img.exists())
                        cover_img.delete();
                    FileUtils.copyInputStreamToFile(new_img.content(), new File(UPLOAD_FOLDER + IMAGES_FOLDER + file_name));
                }
            }catch (Exception q){
                System.out.println(q.getMessage());
            }
        }
        return updated_book;
    }

    public Book deleteBookById(int bookId) throws MyCustumException{
        if(bookId <= 0)
            throw new NigativeNumException();
        Book deleted_book = booksDAO.getBookById(bookId);
        booksDAO.deleteBookById(bookId);
            try {
                if(isDefaultImage(deleted_book.getCover_img()))
                    return deleted_book;
                var arr = deleted_book.getCover_img().split("/");
                String img_name = arr[arr.length-1];
                var cover_img = new File(UPLOAD_FOLDER + IMAGES_FOLDER + img_name);
                if(cover_img.exists())
                    cover_img.delete();
            }catch (Exception q){
                System.out.println(q.getMessage());
            }
            return deleted_book;
    }

    public JSONArray getBooksByUserId(int userId) throws MyCustumException{
        if(userId <= 0)
            throw new NigativeNumException();
        return booksDAO.getBooksByUserId(userId);
    }

    public JSONArray getMostKBooks(int k) throws MyCustumException{
        if(k <= 0)
            throw new NigativeNumException();
        return booksDAO.getMostKBooks(k);
    }

    private boolean isDefaultImage(String cover_img){
        return cover_img.endsWith(DEFAULT_COVER_IMAGE);
    }
}
