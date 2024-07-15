package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;

import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import org.example.exceptions.MyCustumException;
import org.example.model.Book;
import org.example.model.User;
import org.apache.commons.io.FileUtils;
import org.example.service.BooksService;
import org.example.service.UsersService;

import java.io.File;
import java.sql.Date;
import java.util.List;

import org.json.JSONObject;

import static org.example.util.StaticFilesUtil.*;

public class BooksStoreController {
    private BooksService booksService;
    private UsersService usersService;

    private ObjectMapper mapper;

    public BooksStoreController(){
        this.booksService = new BooksService();
        this.usersService = new UsersService();
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public BooksStoreController(BooksService booksService, UsersService usersService){
        this.booksService = booksService;
        this.usersService = usersService;
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.staticFiles.add(staticFiles -> {             // change to host files on a subpath, like '/assets'
                staticFiles.directory = UPLOAD_FOLDER;              // the directory where your files are located
                staticFiles.location = Location.EXTERNAL;      // Location.CLASSPATH (jar) or Location.EXTERNAL (file system)
            });
        });

        app.get("users", this::getAllUsers);
        app.get("users/{id}", this::getUserById);
        app.get("users/{id}/books", this::getBooksByUserId);
        app.post("users", this::registerNewUser);
        app.patch("users/{id}", this::updateUserById);
        app.delete("users/{id}", this::deleteUserById);

        app.get("books", this::getAllBooks);
        app.get("books/most_selling/{k}", this::getMostKBooks);
        app.get("books/most_selling", this::getMostKBooks);
        app.get("books/{id}", this::getBookById);
        app.get("books/{id}/users", this::getUsersByBookId);
        app.post("books", this::addBook);
        app.patch("books/{id}", this::updateBookById);
        app.delete("books/{id}", this::deleteBookById);

        return app;
    }



    private void getAllUsers(Context context){
        try{
            List<User> users = usersService.getAllUsers();
            context.json(mapper.writeValueAsString(users));
            return;
        }catch(JsonProcessingException e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    private void getUserById(Context context) {
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));
            User user = usersService.getUserById(user_id);
            context.json(mapper.writeValueAsString(user));
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void updateUserById(Context context){
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));

            JSONObject jsonBody = new JSONObject(context.body());

            User updated_user = usersService.updateUserById(user_id, jsonBody);
            context.json(mapper.writeValueAsString(updated_user));
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void deleteUserById(Context context){
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));
            User deleted_user = usersService.deleteUserById(user_id);
            context.json(mapper.writeValueAsString(deleted_user));
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void registerNewUser(Context context){
        try{
            User user = mapper.readValue(context.body(), User.class);
            User registeredUser = usersService.addUser(user);
            context.json(mapper.writeValueAsString(registeredUser));
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void getUsersByBookId(Context context){
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));
            var jsonArr = usersService.getUsersByBookId(book_id);
            context.json(jsonArr.toString());
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    //============================ Books ====================================

    private void addBook(Context context){
        try{
            if(context.isMultipart()) {
                var formParams = context.formParamMap();
                Book book = new Book();
                if (formParams.containsKey("title"))
                    book.setTitle(context.formParam("title"));
                if (formParams.containsKey("author_name"))
                    book.setAuthor_name(context.formParam("author_name"));
                if (formParams.containsKey("category"))
                    book.setCategory(context.formParam("category"));
                if (formParams.containsKey("summary"))
                    book.setSummary(context.formParam("summary"));
                if (formParams.containsKey("year"))
                    book.setYear(Date.valueOf(context.formParam("year")));
                if (formParams.containsKey("price"))
                    book.setPrice(Double.parseDouble(context.formParam("price")));

                var cover_img = context.uploadedFile("cover_img");
                String file_name = null;
                if (cover_img != null && cover_img.size()>0) {
                    file_name = IMAGES_FOLDER + book.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + cover_img.extension();
                    book.setCover_img(context.host() + file_name);
                }
                else
                    book.setCover_img(context.host() + DEFAULT_COVER_IMAGE);

                Book addedBook = booksService.addBook(book);
                if (cover_img != null && cover_img.size()>0)
                    FileUtils.copyInputStreamToFile(cover_img.content(), new File(UPLOAD_FOLDER + file_name));
                context.json(mapper.writeValueAsString(addedBook)).status(201);
            }

            else{
                Book book = mapper.readValue(context.body(), Book.class);
                book.setCover_img(context.host() + DEFAULT_COVER_IMAGE);
                Book addedBook = booksService.addBook(book);
                context.json(mapper.writeValueAsString(addedBook));
            }
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void getAllBooks(Context context){
        try{
            List<Book> books = booksService.getAllBooks();
            context.json(mapper.writeValueAsString(books));
            return;
        }catch(JsonProcessingException e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    private void getBookById(Context context){
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));
            Book book = booksService.getBookById(book_id);
            context.json(mapper.writeValueAsString(book));
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
//        catch(NumberFormatException e){
//            context.json("{message:ID must be number}").status(400);}
        catch(Exception e){
            System.out.println(e.getMessage());
            context.status(400);
        }
    }

    private void updateBookById(Context context){
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));

            if(context.isMultipart()) {
                var formParams = context.formParamMap();
                JSONObject jsonBody = new JSONObject();
                if (formParams.containsKey("title"))
                    jsonBody.put("title", context.formParam("title"));
                if (formParams.containsKey("author_name"))
                    jsonBody.put("author_name", context.formParam("author_name"));
                if (formParams.containsKey("category"))
                    jsonBody.put("category", context.formParam("category"));
                if (formParams.containsKey("summary"))
                    jsonBody.put("summary", context.formParam("summary"));
                if (formParams.containsKey("year"))
                    jsonBody.put("year", Date.valueOf(context.formParam("year")));
                if (formParams.containsKey("price"))
                    jsonBody.put("price", Double.parseDouble(context.formParam("price")));

                var cover_img = context.uploadedFile("cover_img");
                if (cover_img != null && cover_img.size() > 0) {
                    jsonBody.put("cover_img",context.host());
//                    jsonBody.put("cover_img","");
                }

                Book updated_book = booksService.updateBookById(book_id, jsonBody, cover_img);
                context.json(mapper.writeValueAsString(updated_book)).status(200);
            }
            else{
                JSONObject jsonBody = new JSONObject(context.body());

                Book updated_book = booksService.updateBookById(book_id, jsonBody, null);
                context.json(mapper.writeValueAsString(updated_book));
            }
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void deleteBookById(Context context){
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));
            Book deleted_book = booksService.deleteBookById(book_id);
            context.json(mapper.writeValueAsString(deleted_book));
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void getBooksByUserId(Context context) {
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));
            var books = booksService.getBooksByUserId(user_id);
            context.json(books.toString());
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void getMostKBooks(Context context) {
        try{
            int k = 3;
            if(context.pathParamMap().containsKey("k")){
                k = Integer.parseInt(context.pathParam("k"));
            }
            var books = booksService.getMostKBooks(k);
            context.json(books.toString());
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }
}
