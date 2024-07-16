package org.example.exceptions;

import io.javalin.http.HttpStatus;

public class InvalidTokenException extends MyCustumException{
    public InvalidTokenException(){
        super("Invalid Token.");
    }
    public int getStatus() {
        return HttpStatus.UNAUTHORIZED.getCode();
    }
}
