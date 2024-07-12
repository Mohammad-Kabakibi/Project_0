package org.example.exceptions;

public class BookTitleExistsException extends MyCustumException{
    public BookTitleExistsException(){
        super("Book Title Already Exists.");
    }
}
