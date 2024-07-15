package org.example.exceptions;

import io.javalin.http.HttpStatus;

public class UserNotFoundException extends MyCustumException{

    public UserNotFoundException(int id){
        super("User (ID:"+id+") Doesn't Exist.");
    }
    public UserNotFoundException(){
        super("User Doesn't Exist.");
    }

    public int getStatus() {
        return HttpStatus.NOT_FOUND.getCode();
    }
}
