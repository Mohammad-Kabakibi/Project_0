package org.example.exceptions;

public class InvalidDateException extends MyCustumException{
    public InvalidDateException(String msg){
        super(msg);
    }
    public InvalidDateException(){
        super("Invalid Date Please Use [yyyy-mm-dd] Format.");
    }
}
