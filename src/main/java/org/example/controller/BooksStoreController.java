package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.config.Result;
import io.javalin.Javalin;

import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import org.example.model.Book;
import org.example.model.User;
import org.apache.commons.io.FileUtils;
import org.example.service.BooksService;
import org.example.service.UsersService;

import java.io.File;
import java.sql.Date;
import java.util.List;

import org.json.JSONObject;

public class BooksStoreController {
    private BooksService booksService;
    private UsersService usersService;

    public BooksStoreController(){

    }

    public BooksStoreController(BooksService booksService, UsersService usersService){
        this.booksService = booksService;
        this.usersService = usersService;
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.staticFiles.add(staticFiles -> {             // change to host files on a subpath, like '/assets'
                staticFiles.directory = "upload";              // the directory where your files are located
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
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            List<User> users = usersService.getAllUsers();
            context.json(mapper.writeValueAsString(users));
            return;
        }catch(JsonProcessingException e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    private void getUserById(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));
            Result<User> user = usersService.getUserById(user_id);
            if(user.getObj() != null){
                context.json(mapper.writeValueAsString(user.getObj()));
            }
            else{
                context.json("{msg:"+user.getMsg()+"}").status(400);
            }
            return;
        }catch(JsonProcessingException e){
            System.out.println(e.getMessage());
        }
        catch(NumberFormatException e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    private void updateUserById(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));

            JSONObject jsonBody = new JSONObject(context.body());

            Result<User> updated_user = usersService.updateUserById(user_id, jsonBody);
            if(updated_user.getObj() != null){
                context.json(mapper.writeValueAsString(updated_user.getObj()));
            }
            else{
                context.json("{msg:"+updated_user.getMsg()+"}").status(400);
            }
            return;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    private void deleteUserById(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));
            Result<User> deleted_user = usersService.deleteUserById(user_id);
            if(deleted_user.getObj() != null){
                context.json(mapper.writeValueAsString(deleted_user.getObj()));
            }
            else{
                context.json("{msg:"+deleted_user.getMsg()+"}").status(400);
            }
            return;
        }catch(JsonProcessingException e){
            System.out.println(e.getMessage());
        }
        catch(NumberFormatException e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    private void registerNewUser(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            User user = mapper.readValue(context.body(), User.class);
            Result<User> registeredUser = usersService.addUser(user);
            if(registeredUser.getObj() == null){
                context.json("{msg:"+registeredUser.getMsg()+"}").status(400);
            }
            else{
                context.json(mapper.writeValueAsString(registeredUser.getObj()));
            }
        }catch(JsonProcessingException e){
            context.status(400);
        }
    }

    private void getUsersByBookId(Context context){
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));
            var jsonArr = usersService.getUsersByBookId(book_id);
            if(jsonArr.getObj() == null){
                context.json("{msg:"+jsonArr.getMsg()+"}").status(400);
            }
            else{
                context.json(jsonArr.getObj().toString());
            }
            return;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    //============================ Books ====================================

    private void addBook(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
                if (cover_img != null) {
                    file_name = "images/" + book.getTitle().replaceAll(" ","_") + cover_img.extension();
                    book.setCover_img(context.host() + "/" + file_name);
                }

                Result<Book> addedBook = booksService.addBook(book);
                if (addedBook.getObj() == null) {
                    context.json("{msg:"+addedBook.getMsg()+"}").status(400);
                } else {
                    if (cover_img != null)
                        FileUtils.copyInputStreamToFile(cover_img.content(), new File("upload/"+file_name));
                    context.json(mapper.writeValueAsString(addedBook.getObj())).status(201);
                }
            }

            else{
                Book book = mapper.readValue(context.body(), Book.class);
                book.setCover_img(context.host() + "/images/default_cover_img.jpg");
                Result<Book> addedBook = booksService.addBook(book);
                if(addedBook.getObj() == null){
                    context.json("{msg:"+addedBook.getMsg()+"}").status(400);
                }
                else{
                    context.json(mapper.writeValueAsString(addedBook.getObj()));
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            context.status(400);
        }
    }

    private void getAllBooks(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));
            Result<Book> book = booksService.getBookById(book_id);
            if(book.getObj() != null){
                context.json(mapper.writeValueAsString(book.getObj()));
            }
            else{
                context.json("{msg:"+book.getMsg()+"}").status(400);
            }
            return;
        }catch(JsonProcessingException e){
            System.out.println(e.getMessage());
        }
        catch(NumberFormatException e){
            System.out.println(e.getMessage());
            context.json("{msg:ID must be number}").status(400);
        }
        context.status(400);
    }

    private void updateBookById(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
                String file_name = null;
                if (cover_img != null) {
                    file_name = "images/" + jsonBody.getString("title").replaceAll(" ","_") + cover_img.extension();
                    jsonBody.put("cover_img",context.host() + "/" + file_name);
                }

                Result<Book> updated_book = booksService.updateBookById(book_id, jsonBody);
                if (updated_book.getObj() == null) {
                    context.json("{msg:"+updated_book.getMsg()+"}").status(400);
                } else {
                    if (cover_img != null)
                        FileUtils.copyInputStreamToFile(cover_img.content(), new File("upload/" + file_name));
                    context.json(mapper.writeValueAsString(updated_book.getObj())).status(200);
                }
            }
            else{
                JSONObject jsonBody = new JSONObject(context.body());

                Result<Book> updated_book = booksService.updateBookById(book_id, jsonBody);
                if (updated_book.getObj() != null) {
                    context.json(mapper.writeValueAsString(updated_book.getObj()));
                } else {
                    context.json("{msg:" + updated_book.getMsg() + "}").status(400);
                }
                return;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    private void deleteBookById(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));
            Result<Book> deleted_book = booksService.deleteBookById(book_id);
            if(deleted_book.getObj() != null){
                context.json(mapper.writeValueAsString(deleted_book.getObj()));
            }
            else{
                context.json("{msg:"+deleted_book.getMsg()+"}").status(400);
            }
            return;
        }catch(JsonProcessingException e){
            System.out.println(e.getMessage());
        }
        catch(NumberFormatException e){
            System.out.println(e.getMessage());
        }
        context.status(400);
    }

    private void getBooksByUserId(Context context) {
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));
            var books = booksService.getBooksByUserId(user_id);
            if(books.getObj() == null){
                context.json("{msg:"+books.getMsg()+"}").status(400);
            }
            else{
                context.json(books.getObj().toString());
            }
            return;
        }catch(Exception q){
            System.out.println(q.getMessage());
        }
        context.status(400);
    }

    private void getMostKBooks(Context context) {
        try{
            int k = 3;
            if(context.pathParamMap().containsKey("k")){
                k = Integer.parseInt(context.pathParam("k"));
            }
            var books = booksService.getMostKBooks(k);
            if(books.getObj() == null){
                context.json("{msg:"+books.getMsg()+"}").status(400);
            }
            else{
                context.json(books.getObj().toString());
            }
            return;
        }catch(Exception q){
            System.out.println(q.getMessage());
        }
        context.status(400);
    }
}
