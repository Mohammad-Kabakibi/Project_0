package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;

import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import model.Book;
import model.User;
import org.apache.commons.io.FileUtils;
import service.BooksService;
import service.UsersService;

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
            User user = usersService.getUserById(user_id);
            if(user != null){
                context.json(mapper.writeValueAsString(user));
            }
            else{
                context.status(404);
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

            User updated_user = usersService.updateUserById(user_id, jsonBody);
            if(updated_user != null){
                context.json(mapper.writeValueAsString(updated_user));
            }
            else{
                context.status(400);
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
            User deleted_user = usersService.deleteUserById(user_id);
            if(deleted_user != null){
                context.json(mapper.writeValueAsString(deleted_user));
            }
            else{
                context.status(200);
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
            User registeredUser = usersService.addUser(user);
            if(registeredUser == null){
                context.status(400);
            }
            else{
                context.json(mapper.writeValueAsString(registeredUser));
            }
        }catch(JsonProcessingException e){
            context.status(400);
        }
    }

    private void getUsersByBookId(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));
            var jsonArr = usersService.getUsersByBookId(book_id);

            context.json(jsonArr.toString());
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
            var formParams = context.formParamMap();
            Book book = new Book();
            if(formParams.containsKey("title"))
                book.setTitle(context.formParam("title"));
            if(formParams.containsKey("author_name"))
                book.setAuthor_name(context.formParam("author_name"));
            if(formParams.containsKey("category"))
                book.setCategory(context.formParam("category"));
            if(formParams.containsKey("summary"))
                book.setSummary(context.formParam("summary"));
            if(formParams.containsKey("year"))
                book.setYear(Date.valueOf(context.formParam("year")));
            if(formParams.containsKey("price"))
                book.setPrice(Double.parseDouble(context.formParam("price")));

            var cover_img = context.uploadedFile("cover_img");
            String file_name = null;
            if(cover_img != null) {
                file_name = "images/" + book.getTitle() + cover_img.extension();
                book.setCover_img(context.host()+"/"+file_name);
            }

            Book addedBook = booksService.addBook(book);
            if(addedBook == null){
                context.status(400);
            }
            else{
                if(cover_img != null)
                    FileUtils.copyInputStreamToFile(cover_img.content(), new File(file_name));
                context.json(mapper.writeValueAsString(addedBook)).status(201);
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
            Book book = booksService.getBookById(book_id);
            if(book != null){
                context.json(mapper.writeValueAsString(book));
            }
            else{
                context.status(404);
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

    private void updateBookById(Context context){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            int book_id = Integer.parseInt(context.pathParam("id"));

            JSONObject jsonBody = new JSONObject(context.body());

            Book updated_book = booksService.updateBookById(book_id, jsonBody);
            if(updated_book != null){
                context.json(mapper.writeValueAsString(updated_book));
            }
            else{
                context.status(400);
            }
            return;
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
            Book deleted_book = booksService.deleteBookById(book_id);
            if(deleted_book != null){
                context.json(mapper.writeValueAsString(deleted_book));
            }
            else{
                context.status(200);
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
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try{
            int user_id = Integer.parseInt(context.pathParam("id"));
            var books = booksService.getBooksByUserId(user_id);
            context.json(books.toString());
            return;
        }catch(Exception q){
            System.out.println(q.getMessage());
        }
        context.status(400);
    }
}
