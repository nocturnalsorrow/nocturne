package com.danialrekhman.orderservicenorcurne.exception;

public class InvalidOrderItemDataException extends RuntimeException {
    public InvalidOrderItemDataException(String message) {
        super(message);
    }
}
