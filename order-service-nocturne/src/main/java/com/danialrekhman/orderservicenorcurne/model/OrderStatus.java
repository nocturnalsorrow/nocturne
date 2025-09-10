package com.danialrekhman.orderservicenorcurne.model;

public enum OrderStatus {
    NEW,                // New order just placed
    WAITING_FOR_PAYMENT,   // Waiting for payment (e.g. manual payment)
    PAID,               // Payment received, waiting for processing
    PROCESSING,         // Order is being prepared (e.g. picked and packed)
    SHIPPED,            // Shipped by delivery service
    IN_TRANSIT,         // In transit (according to tracking)
    DELIVERED,          // Delivered to the customer
    COMPLETED,          // Completed (e.g. confirmed or after delivery)
    CANCELLED,          // Cancelled by customer or store
    RETURN_REQUESTED,   // Return requested (e.g. for warranty or issue)
    RETURNED,           // Item returned and processed
    REFUNDED,           // Money refunded to the customer
    FAILED              // Order failed (e.g. payment or system error)
}
