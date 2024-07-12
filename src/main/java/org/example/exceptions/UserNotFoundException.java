package org.example.exceptions;

public class UserNotFoundException extends MyCustumException{
    public UserNotFoundException(int id){
        super("User (ID:"+id+") Doesn't Exist.");
    }
    public UserNotFoundException(){
        super("User Doesn't Exist.");
    }
}
