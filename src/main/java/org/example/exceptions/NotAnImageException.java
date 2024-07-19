package org.example.exceptions;

public class NotAnImageException extends MyCustumException{
    public NotAnImageException(){
        super("Please Choose An Image File.");
    }
}
