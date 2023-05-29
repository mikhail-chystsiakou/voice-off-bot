package org.example.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.EntityAlreadyExistsException;
import org.example.exception.EntityNotFoundException;
import org.example.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({EntityAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleEntityAlreadyExistsException(Exception exception) {
        return createResponseFromHttpCodeAndMessage(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(Exception exception) {
        return createResponseFromHttpCodeAndMessage(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        FieldError error = exception.getFieldError();
        assert error != null;
        return createResponseFromHttpCodeAndMessage(HttpStatus.BAD_REQUEST, error.getDefaultMessage());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Got and exception", exception);
        return createResponseFromHttpCodeAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> createResponseFromHttpCodeAndMessage(HttpStatus status, String exceptionMessage) {
        ErrorResponse body = ErrorResponse.builder()
                .code(status.value())
                .message(exceptionMessage)
                .build();
        return new ResponseEntity<>(body, status);
    }
}
