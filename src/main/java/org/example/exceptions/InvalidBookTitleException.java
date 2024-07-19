package org.example.exceptions;

public class InvalidBookTitleException extends MyCustumException{
    public InvalidBookTitleException(){
        super("Invalid Book Title.");
    }
    public InvalidBookTitleException(String msg){
        super(msg);
    }
}
