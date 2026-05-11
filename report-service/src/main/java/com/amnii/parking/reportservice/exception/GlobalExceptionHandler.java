package com.amnii.parking.reportservice.exception;

import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {
    record Err(boolean success, String message, List<String> errors) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Err> validation(MethodArgumentNotValidException ex) {
        var errs = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
        return ResponseEntity.badRequest().body(new Err(false, "Validation failed", errs));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Err> bad(BadRequestException ex) {
        return ResponseEntity.badRequest().body(new Err(false, ex.getMessage(), null));
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Err> conflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Err(false, ex.getMessage(), null));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Err> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Err(false, ex.getMessage(), null));
    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Err> forbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Err(false, ex.getMessage(), null));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Err> general(Exception ex) {
        log.error("Unhandled: ", ex);
        return ResponseEntity.internalServerError().body(new Err(false, "Internal server error", null));
    }
}
