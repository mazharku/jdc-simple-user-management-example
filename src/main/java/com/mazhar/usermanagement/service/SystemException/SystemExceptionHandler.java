package com.mazhar.usermanagement.service.SystemException;


import com.mazhar.usermanagement.model.dto.ErrorMessage;
import com.mazhar.usermanagement.model.dto.exceptions.InvalidRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class SystemExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(SystemExceptionHandler.class);

    @ExceptionHandler({
            InvalidRequestType.class
    })
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMessage badRequestHandler(RuntimeException ex) {
        log.error(Arrays.toString(ex.getStackTrace()));
        return new ErrorMessage(ex.getMessage());
    }

    @ExceptionHandler({
            RuntimeException.class
    })
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorMessage notFoundHandler(RuntimeException ex) {
        log.error(Arrays.toString(ex.getStackTrace()));
        return new ErrorMessage(ex.getMessage());
    }

    @ExceptionHandler({
            Exception.class
    })
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage unhandled(Exception ex) {
        log.error(Arrays.toString(ex.getStackTrace()));
        return new ErrorMessage(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorMessage handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" ; "));

        return new ErrorMessage(errorMessage);
    }

}
