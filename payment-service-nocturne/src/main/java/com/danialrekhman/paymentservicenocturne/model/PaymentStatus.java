package com.danialrekhman.paymentservicenocturne.model;

public enum PaymentStatus {
    PENDING,    // ожидает подтверждения
    SUCCESS,    // успешно оплачен
    FAILED      // ошибка платежа
}
