package org.example.exceptions;

public class BookNotFoundException extends MyCustumException{
    public BookNotFoundException(int id){
        super("Book (ID:"+id+") Doesn't Exist.");
    }
    public BookNotFoundException(){
        super("Book Doesn't Exist.");
    }
}
