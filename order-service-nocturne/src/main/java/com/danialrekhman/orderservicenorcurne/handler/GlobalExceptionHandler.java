package com.danialrekhman.orderservicenorcurne.handler;

import com.danialrekhman.orderservicenorcurne.dto.ApiError;
import com.danialrekhman.orderservicenorcurne.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handleOrderNotFoundException(OrderNotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Order not found", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(OrderItemNotFoundException.class)
    public ResponseEntity<Object> handleOrderItemNotFoundException(OrderItemNotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Order item not found", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(CustomAccessDeniedException.class)
    public ResponseEntity<Object> handleCustomAccessDeniedException(CustomAccessDeniedException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<Object> handleInvalidQuantityException(InvalidQuantityException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid quantity", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(InvalidOrderItemDataException.class)
    public ResponseEntity<Object> handleInvalidOrderItemDataException(InvalidOrderItemDataException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid order item data", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<Object> handleOrderCancellationException(OrderCancellationException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Order cannot be canceled", ex.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    // Обработчик для всех остальных непредвиденных исключений
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getLocalizedMessage());
        // В реальном приложении здесь обязательно должно быть логирование ошибки
        // log.error("Unhandled exception occurred:", ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
