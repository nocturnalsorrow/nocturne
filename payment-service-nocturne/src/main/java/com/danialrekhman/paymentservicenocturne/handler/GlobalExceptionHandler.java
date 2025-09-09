package com.danialrekhman.paymentservicenocturne.handler;

import com.danialrekhman.paymentservicenocturne.dto.ApiError;
import com.danialrekhman.paymentservicenocturne.exception.CustomAccessDeniedException;
import com.danialrekhman.paymentservicenocturne.exception.InvalidPaymentDataException;
import com.danialrekhman.paymentservicenocturne.exception.PaymentNotFoundException;
import com.danialrekhman.paymentservicenocturne.exception.PaymentProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomAccessDeniedException.class)
    public ResponseEntity<Object> handleCustomAccessDeniedException(CustomAccessDeniedException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Object> handlePaymentNotFoundException(PaymentNotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Payment not found", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(InvalidPaymentDataException.class)
    public ResponseEntity<Object> handleInvalidPaymentDataException(InvalidPaymentDataException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid payment data", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<Object> handlePaymentProcessingException(PaymentProcessingException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Payment processing failed", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getLocalizedMessage());
        // In a real project: log.error("Unhandled exception:", ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}