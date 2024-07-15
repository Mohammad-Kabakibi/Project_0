package org.example.exceptions;

import io.javalin.http.HttpStatus;

public class BookNotFoundException extends MyCustumException{

    public BookNotFoundException(int id){
        super("Book (ID:"+id+") Doesn't Exist.");
    }
    public BookNotFoundException(){
        super("Book Doesn't Exist.");
    }

    public int getStatus() {
        return HttpStatus.NOT_FOUND.getCode();
    }
}
