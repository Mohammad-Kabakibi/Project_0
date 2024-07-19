package org.example;

import io.javalin.Javalin;
import org.example.controller.BooksStoreController;
import org.example.service.BooksService;
import org.example.service.UsersService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        BooksStoreController controller = new BooksStoreController(new BooksService(), new UsersService());
        Javalin app = controller.startAPI();
        app.start(8080);
    }
}