package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;

import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import org.example.exceptions.*;
import org.example.model.Book;
import org.example.model.User;
import org.apache.commons.io.FileUtils;
import org.example.service.BooksService;
import org.example.service.UsersService;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
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

        app.before("app/*",this::authorize);
        app.before("images/*",this::authorize);
        app.get("nothing",context -> {}); // returns nothing to keep the data from the Unauthorized exception.

        app.get("login", context -> context.result("login page..."));
        app.post("login", this::login);

        app.get("app/users", this::getAllUsers);
        app.get("app/users/{id}", this::getUserById);
        app.get("app/users/after/{date}", this::getUsersByDateAfter);
        app.get("app/users/before/{date}", this::getUsersByDateBefore);
        app.get("app/users/{id}/books", this::getBooksByUserId);
        app.post("app/users", this::registerNewUser);
        app.patch("app/users/{id}", this::updateUserById);
        app.delete("app/users/{id}", this::deleteUserById);

        app.get("app/books", this::getAllBooks);
        app.get("app/books/most_selling/{k}", this::getMostKBooks);
        app.get("app/books/most_selling", this::getMostKBooks);
        app.get("app/books/most_selling/{k}/after/{date}", this::getMostKBooksAfter);
        app.get("app/books/most_selling/after/{date}", this::getMostKBooksAfter);
        app.get("app/books/most_selling/{k}/before/{date}", this::getMostKBooksBefore);
        app.get("app/books/most_selling/before/{date}", this::getMostKBooksBefore);
        app.get("app/books/most_selling/between/{date1}/{date2}", this::getMostKBooksBetween);
        app.get("app/books/most_selling/{k}/between/{date1}/{date2}", this::getMostKBooksBetween);
        app.get("app/books/{id}", this::getBookById);
        app.get("app/books/{id}/users", this::getUsersByBookId);
        app.get("app/books/before/{date}", this::getBooksByDateBefore);
        app.get("app/books/after/{date}", this::getBooksByDateAfter);
        app.post("app/books", this::addBook);
        app.patch("app/books/{id}", this::updateBookById);
        app.delete("app/books/{id}", this::deleteBookById);

        return app;
    }


    private void login(Context context) {
        // this is a sample login using the concept of tokens
        try {
            var jo = new JSONObject(context.body());
            if (jo.has("username") && jo.has("password")) {
                if (jo.getString("username").equals("admin") && jo.getString("password").equals("123")) {
                    var token_object = new JSONObject();
                    // in reality, it should be hashing with a key... this is just a test
                    String token = new String(Base64.getEncoder().encode(jo.getString("username").getBytes()), "UTF-8");
                    token_object.put("token", token); // should be in database
                    tokens.put(jo.getString("username"), token);
                    context.json(token_object.toString());
                }
                else
                    throw new InvalidCredentialsException();
            }
            else
                throw new MissingCredentialsException();
        }catch (JSONException | UnsupportedEncodingException q){
            context.status(400);
        }catch (MyCustumException q){
            context.json(q.getMessage()).status(q.getStatus());
        }
    }

    private void authorize(Context context){
        try {
            if (context.headerMap().containsKey("Authorization")) {
                String[] arr = context.headerMap().get("Authorization").split(" ");
                if(arr.length < 2)
                    throw new InvalidTokenFormatException(); // should be [ Bearer <token> ]
                String token = arr[1];
//                if (isValidToken(token))
//                    return;
                isValidToken(token);
            }
            else
                throw new UnAuthorizedException();
        }catch (MyCustumException q){
            context.redirect("/nothing"); // to prevent running the endpoint
            context.json(q.getMessage()).status(q.getStatus());
        }
    }

    Map<String, String> tokens = new HashMap<>();

    private boolean isValidToken(String token) throws InvalidTokenException {
        for(String t : tokens.values()){
            if(t.equals(token))
                return true;
        }
//        return false;
        throw new InvalidTokenException();
    }


    //============================ Users ====================================

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

    private void getUsersByDateAfter(Context context) {
        try{
            Date date = valueOf(context.pathParam("date"));
            var users = usersService.getUsersByDateAfter(date);
            context.json(users.toString());
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void getUsersByDateBefore(Context context) {
        try{
            Date date = valueOf(context.pathParam("date"));
            var users = usersService.getUsersByDateBefore(date);
            context.json(users.toString());
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
                context.json(mapper.writeValueAsString(addedBook)).status(201);
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
            usersService.getUserById(user_id);
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

    private Date valueOf(String date) throws InvalidDateException {
        try{
            return Date.valueOf(date);
        }catch (Exception q){
            throw new InvalidDateException();
        }
    }

    private void getBooksByDateAfter(Context context) {
        try{
            Date date = valueOf(context.pathParam("date"));
            var books = booksService.getBooksByDateAfter(date);
            context.json(mapper.writeValueAsString(books));
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void getBooksByDateBefore(Context context) {
        try{
            Date date = valueOf(context.pathParam("date"));
            var books = booksService.getBooksByDateBefore(date);
            context.json(mapper.writeValueAsString(books));
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }

    private void getMostKBooksAfter(Context context) {
        try{
            int k = 3;
            if(context.pathParamMap().containsKey("k")){
                k = Integer.parseInt(context.pathParam("k"));
            }
            Date date = valueOf(context.pathParam("date"));
            var books = booksService.getMostKBooksAfter(date, k);
            context.json(books.toString());
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }
    private void getMostKBooksBefore(Context context) {
        try{
            int k = 3;
            if(context.pathParamMap().containsKey("k")){
                k = Integer.parseInt(context.pathParam("k"));
            }
            Date date = valueOf(context.pathParam("date"));
            var books = booksService.getMostKBooksBefore(date, k);
            context.json(books.toString());
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }
    private void getMostKBooksBetween(Context context) {
        try{
            int k = 3;
            if(context.pathParamMap().containsKey("k")){
                k = Integer.parseInt(context.pathParam("k"));
            }
            Date date1 = valueOf(context.pathParam("date1"));
            Date date2 = valueOf(context.pathParam("date2"));
            var books = booksService.getMostKBooksBetween(date1, date2, k);
            context.json(books.toString());
        }catch(MyCustumException e){
            context.json(e.getMessage()).status(e.getStatus());
        }
        catch(Exception e){
            context.status(400);
        }
    }
}
