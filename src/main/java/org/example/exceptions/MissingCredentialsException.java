package org.example.exceptions;

import io.javalin.http.HttpStatus;

public class MissingCredentialsException extends MyCustumException{
    public MissingCredentialsException(){
        super("Missing Credentials.");
    }
    public int getStatus() {
        return HttpStatus.UNAUTHORIZED.getCode();
    }
}
