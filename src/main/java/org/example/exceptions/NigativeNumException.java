package org.example.exceptions;

public class NigativeNumException extends MyCustumException{
    public NigativeNumException(){
        super("Please Enter a Positive Number.");
    }
}
