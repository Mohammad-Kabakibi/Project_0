package org.example.exceptions;

import io.javalin.http.HttpStatus;

public class InvalidCredentialsException extends MyCustumException {
    public InvalidCredentialsException(){
        super("Invalid Credentials.");
    }
    public int getStatus() {
        return HttpStatus.UNAUTHORIZED.getCode();
    }
}
