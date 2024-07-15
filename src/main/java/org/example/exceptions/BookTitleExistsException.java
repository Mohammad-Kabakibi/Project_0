package org.example.exceptions;

import io.javalin.http.HttpStatus;

public class BookTitleExistsException extends MyCustumException{
    public BookTitleExistsException(){
        super("Book Title Already Exists.");
    }

    public int getStatus() {
        return HttpStatus.CONFLICT.getCode();
    }
}
