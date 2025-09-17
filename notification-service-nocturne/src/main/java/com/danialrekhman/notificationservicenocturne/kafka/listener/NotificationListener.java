package com.danialrekhman.notificationservicenocturne.kafka.listener;

import com.danialrekhman.commonevents.*;
import com.danialrekhman.notificationservicenocturne.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final EmailService emailService;

    @KafkaListener(
            topics = "user-registered",
            groupId = "notification-service-group",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("Received UserRegisteredEvent: {}", event.getEmail());
            emailService.sendEmail(
                    event.getEmail(),
                    "Welcome to Our Music Shop!",
                    "Hello " + event.getUsername() + ",\n\n" +
                            "Thank you for registering with us! We're thrilled to have you join our community of music lovers. " +
                            "Discover an incredible assortment of instruments, accessories, albums, and everything music-related. " +
                            "Whether you're a beginner or a pro, we've got something for everyone.\n\n" +
                            "Happy shopping, and let the music play!\n\n" +
                            "Best regards,\n" +
                            "Your Nocturne Music Shop Team"
            );
        } catch (Exception e) {
            log.error("Error processing UserRegisteredEvent for email: {}", event.getEmail(), e);
        }
    }

    @KafkaListener(
            topics = "order-created",
            groupId = "notification-service-group",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, email={}", event.getOrderId(), event.getUserEmail());
        emailService.sendEmail(
                event.getUserEmail(),
                "Order Created",
                "Your order #" + event.getOrderId() + " was created. Total: " + event.getTotalPrice() + "$");
    }

    @KafkaListener(
            topics = "payment-processed",
            groupId = "notification-service-group",
            containerFactory = "paymentProcessedKafkaListenerContainerFactory"
    )
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent: orderId={}, status={}, email={}",
                event.getOrderId(), event.getStatus(), event.getUserEmail());

        String body = "Order #" + event.getOrderId() +
                " | Payment " + event.getStatus() +
                " | Amount: " + event.getAmount() + " $" +
                " | Method: " + event.getMethod();

        emailService.sendEmail(event.getUserEmail(), "Payment Result", body);
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "notification-service-group",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent: orderId={}, reason={}, email={}",
                event.getOrderId(), event.getReason(), event.getUserEmail());

        emailService.sendEmail(
                event.getUserEmail(),
                "Payment Failed",
                "Your payment for order #" + event.getOrderId() + " failed. Reason: " + event.getReason()
        );
    }

    @KafkaListener(topics = "email-verification", groupId = "notification-service-group",
            containerFactory = "verificationKafkaListenerContainerFactory")
    public void onVerificationEmail(VerificationEmailEvent event) {
        String to = event.getEmail();
        String subject = "Please verify your email";
        String body = "Hello " + event.getUsername() + ",\n\n"
                + "We received a request to verify your email address.\n\n"
                + "Copy and paste the following verification code into the browser:\n\n"
                + event.getToken() + "\n\n"
                + "or click the link to verify your email:\n\n"
                + event.getVerificationUrl()
                + "\n\nIf you didn't register, ignore this email.";
        emailService.sendEmail(to, subject, body);
    }
}
