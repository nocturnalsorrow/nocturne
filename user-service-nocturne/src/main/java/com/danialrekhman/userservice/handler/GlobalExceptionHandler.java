package com.danialrekhman.userservice.handler;

import com.danialrekhman.userservice.dto.ApiError;
import com.danialrekhman.userservice.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 401 Unauthorized - Wrong login or password
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiError> handleAuthenticationFailed(AuthenticationFailedException ex) {
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, "Authentification error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 403 Forbidden
    @ExceptionHandler(CustomAccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(CustomAccessDeniedException ex) {
        ApiError error = new ApiError(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // 404 Not Found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND, "User not found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 409 Conflict - Conflict (email duplicate, for example)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResource(DuplicateResourceException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT, "Resource already exist", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // 422 Unprocessable Entity
    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ApiError> handleInvalidUserData(InvalidUserDataException ex) {
        ApiError error = new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid user data", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // 500 Internal Server Error
    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<ApiError> handleImageProcessing(ImageProcessingException ex) {
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Image processing error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
