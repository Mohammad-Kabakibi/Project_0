package org.example.exceptions;

import io.javalin.http.HttpStatus;

public class UnAuthorizedException extends MyCustumException{
    public UnAuthorizedException(){
        super("Unauthorized.");
    }

    public int getStatus() {
        return HttpStatus.UNAUTHORIZED.getCode();
    }
}
