package com.danialrekhman.productservicenocturne.handler;

import com.danialrekhman.productservicenocturne.dto.ApiError;
import com.danialrekhman.productservicenocturne.exception.CustomAccessDeniedException;
import com.danialrekhman.productservicenocturne.exception.DuplicateResourceException;
import com.danialrekhman.productservicenocturne.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomAccessDeniedException.class)
    public ResponseEntity<Object> handleCustomAccessDeniedException(CustomAccessDeniedException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResourceException(DuplicateResourceException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT, "Data conflict", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    // Этот обработчик перехватывает ошибки, когда вы пытаетесь удалить, например,
    // категорию, на которую еще ссылаются продукты.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT, "Data integrity exception", "Cannot delete resource due to related data.");
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Incorrect request", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        // Обязательно логируйте такие ошибки!
        // log.error("Unhandled exception occurred:", ex);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getLocalizedMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}