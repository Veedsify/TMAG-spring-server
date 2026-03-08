package com.TravelMedicineAdvisory.Server.core.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern UNIQUE_CONSTRAINT_PATTERN = Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\) already exists");

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.warn("Data integrity violation: {}", ex.getMessage());
        String message = "A record with the provided data already exists.";

        String rootMessage = ex.getMostSpecificCause().getMessage();
        if (rootMessage != null) {
            Matcher matcher = UNIQUE_CONSTRAINT_PATTERN.matcher(rootMessage);
            if (matcher.find()) {
                String field = matcher.group(1);
                message = "Sorry" + field + " already exists.";
            } else if (rootMessage.contains("not-null constraint")) {
                message = "A required field is missing.";
            }
        }

        return new ResponseEntity<>(new ErrorResponse(message, false, null), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(new ErrorResponse("You do not have permission to perform this action.", false, null), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return new ResponseEntity<>(new ErrorResponse("Invalid email or password.", false, null), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Invalid email or password.", false, null), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        return new ResponseEntity<>(new ErrorResponse("Resource not found.", false, null), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed.");
        return new ResponseEntity<>(new ErrorResponse(message, false, null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(new ErrorResponse("Invalid parameter: " + ex.getName(), false, null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), false, null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getReason(), false, null), HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new ErrorResponse("An internal error occurred. Please try again later.", false, null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
