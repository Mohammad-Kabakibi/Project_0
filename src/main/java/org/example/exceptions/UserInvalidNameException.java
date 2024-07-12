package org.example.exceptions;

public class UserInvalidNameException extends MyCustumException{
    public UserInvalidNameException(){
        super("Name Must Be Between 2-100 Characters");
    }
}
