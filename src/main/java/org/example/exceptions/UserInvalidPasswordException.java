package org.example.exceptions;

public class UserInvalidPasswordException extends MyCustumException{
    public UserInvalidPasswordException(){
        super("Password Must Be Between 4-50 Characters.");
    }
}
