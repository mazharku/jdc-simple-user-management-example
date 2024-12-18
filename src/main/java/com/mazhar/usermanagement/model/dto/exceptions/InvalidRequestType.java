package com.mazhar.usermanagement.model.dto.exceptions;

public class InvalidRequestType extends RuntimeException {
    public InvalidRequestType(String message){
        super(message);
    }
}
