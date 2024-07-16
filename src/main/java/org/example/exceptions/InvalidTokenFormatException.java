package org.example.exceptions;

import io.javalin.http.HttpStatus;

public class InvalidTokenFormatException extends MyCustumException{
    public InvalidTokenFormatException() {
        super("Invalid Token: Make Sure that the value in the header is 'Bearer <token>'.");
    }
    public int getStatus() {
        return HttpStatus.UNAUTHORIZED.getCode();
    }
}
