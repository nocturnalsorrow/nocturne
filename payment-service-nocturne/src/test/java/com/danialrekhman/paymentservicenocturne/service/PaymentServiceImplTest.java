package com.danialrekhman.paymentservicenocturne.service;

import com.danialrekhman.commonevents.OrderCreatedEvent;
import com.danialrekhman.commonevents.PaymentFailedEvent;
import com.danialrekhman.commonevents.PaymentProcessedEvent;
import com.danialrekhman.paymentservicenocturne.dto.PaymentResponseDTO;
import com.danialrekhman.paymentservicenocturne.dto.PaymentStatusRequestDTO;
import com.danialrekhman.paymentservicenocturne.exception.CustomAccessDeniedException;
import com.danialrekhman.paymentservicenocturne.exception.InvalidPaymentDataException;
import com.danialrekhman.paymentservicenocturne.exception.PaymentNotFoundException;
import com.danialrekhman.paymentservicenocturne.exception.PaymentProcessingException;
import com.danialrekhman.paymentservicenocturne.kafka.PaymentEventProducer;
import com.danialrekhman.paymentservicenocturne.mapper.PaymentMapper;
import com.danialrekhman.paymentservicenocturne.model.Payment;
import com.danialrekhman.paymentservicenocturne.model.PaymentMethod;
import com.danialrekhman.paymentservicenocturne.model.PaymentStatus;
import com.danialrekhman.paymentservicenocturne.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer eventProducer;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private OrderCreatedEvent orderCreatedEvent;
    private Payment payment;
    private String userEmail;
    private String adminRole;

    @BeforeEach
    void setUp() {
        userEmail = "user@example.com";
        adminRole = "ROLE_ADMIN";

        orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(1L)
                .userEmail(userEmail)
                .totalPrice(new BigDecimal("100.00"))
                .build();

        payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .userEmail(userEmail)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.SUCCESS)
                .method(PaymentMethod.MOCK)
                .transactionId("some-uuid")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- Тесты для createAndProcessPayment ---

    @Test
    @DisplayName("Должен успешно создать и обработать платеж")
    void createAndProcessPayment_ShouldSucceed() {
        // Arrange
        when(paymentRepository.findByOrderId(orderCreatedEvent.getOrderId())).thenReturn(Collections.emptyList());

        // Настраиваем мок save() так, чтобы он возвращал переданный ему объект.
        // Это эмулирует поведение реального репозитория, который возвращает сохраненную сущность.
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment argument = invocation.getArgument(0);
            // Эмулируем, что после первого сохранения у объекта появляется ID
            if (argument.getId() == null) {
                argument.setId(1L);
            }
            return argument;
        });

        // Act
        paymentService.createAndProcessPayment(orderCreatedEvent);

        // Assert
        // Создаем ArgumentCaptor здесь, на этапе проверки
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        // Проверяем, что save был вызван дважды, и захватываем оба вызова
        verify(paymentRepository, times(2)).save(paymentCaptor.capture());

        List<Payment> capturedPayments = paymentCaptor.getAllValues();

        // Первый вызов save() должен быть с объектом в статусе PENDING
        Payment firstSave = capturedPayments.get(0);
        assertThat(firstSave.getOrderId()).isEqualTo(orderCreatedEvent.getOrderId());
        assertThat(firstSave.getStatus()).isEqualTo(PaymentStatus.PENDING);

        // Второй вызов save() должен быть с объектом в статусе SUCCESS
        Payment secondSave = capturedPayments.get(1);
        assertThat(secondSave.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(secondSave.getUpdatedAt()).isNotNull();

        // Проверяем, что были отправлены правильные события Kafka
        verify(eventProducer, times(1)).publishPaymentProcessed(any(PaymentProcessedEvent.class));
        verify(eventProducer, never()).publishPaymentFailed(any(PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("Должен обработать дублирующий запрос идемпотентно")
    void createAndProcessPayment_ShouldHandleDuplicateRequestIdempotently() {
        // Arrange
        when(paymentRepository.findByOrderId(orderCreatedEvent.getOrderId())).thenReturn(List.of(payment));

        // Act
        paymentService.createAndProcessPayment(orderCreatedEvent);

        // Assert
        verify(paymentRepository, never()).save(any(Payment.class)); // Новый платеж не должен сохраняться
        verify(eventProducer, times(1)).publishPaymentProcessed(any(PaymentProcessedEvent.class));
        verify(eventProducer, never()).publishPaymentFailed(any(PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при ошибке сохранения в репозитории")
    void createAndProcessPayment_ShouldThrowException_WhenRepositoryFails() {
        // Arrange
        when(paymentRepository.findByOrderId(orderCreatedEvent.getOrderId())).thenReturn(Collections.emptyList());
        when(paymentRepository.save(any(Payment.class))).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.createAndProcessPayment(orderCreatedEvent))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("Failed to process payment for orderId " + orderCreatedEvent.getOrderId());
    }

    // --- Тесты для getPaymentEntityById ---

    @Test
    @DisplayName("Должен вернуть платеж для владельца")
    void getPaymentEntityById_ShouldReturnPayment_ForOwner() {
        // Arrange
        when(authentication.getName()).thenReturn(userEmail);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        mockUserRole(false);

        // Act
        Payment foundPayment = paymentService.getPaymentEntityById(1L, authentication);

        // Assert
        assertThat(foundPayment).isNotNull();
        assertThat(foundPayment.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Должен вернуть платеж для администратора")
    void getPaymentEntityById_ShouldReturnPayment_ForAdmin() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        mockUserRole(true); // Пользователь - админ

        // Act
        Payment foundPayment = paymentService.getPaymentEntityById(1L, authentication);

        // Assert
        assertThat(foundPayment).isNotNull();
        assertThat(foundPayment.getId()).isEqualTo(1L);
        verify(authentication, never()).getName(); // Имя не должно проверяться для админа
    }

    @Test
    @DisplayName("Должен выбросить PaymentNotFoundException, если платеж не найден")
    void getPaymentEntityById_ShouldThrowNotFoundException() {
        // Arrange
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentEntityById(99L, authentication))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment with ID 99 not found");
    }

    @Test
    @DisplayName("Должен выбросить CustomAccessDeniedException для не-владельца")
    void getPaymentEntityById_ShouldThrowAccessDenied_ForNonOwner() {
        // Arrange
        when(authentication.getName()).thenReturn("another.user@example.com");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        mockUserRole(false);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentEntityById(1L, authentication))
                .isInstanceOf(CustomAccessDeniedException.class)
                .hasMessageContaining("You don't have access to this payment.");
    }

    // --- Тесты для getPaymentById (DTO) ---

    @Test
    @DisplayName("Должен успешно вернуть PaymentResponseDTO")
    void getPaymentById_ShouldReturnDto() {
        // Arrange
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(1L);

        when(authentication.getName()).thenReturn(userEmail);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(dto);
        mockUserRole(false);

        // Act
        PaymentResponseDTO result = paymentService.getPaymentById(1L, authentication);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(paymentMapper, times(1)).toDto(payment);
    }

    // --- Тесты для getAllPayments ---

    @Test
    @DisplayName("Администратор должен получить все платежи")
    void getAllPayments_ShouldReturnAllPayments_ForAdmin() {
        // Arrange
        mockUserRole(true);
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        // Act
        List<Payment> payments = paymentService.getAllPayments(authentication);

        // Assert
        assertThat(payments).isNotEmpty();
        assertThat(payments).hasSize(1);
    }

    @Test
    @DisplayName("Не-администратор не должен получать все платежи")
    void getAllPayments_ShouldThrowAccessDenied_ForNonAdmin() {
        // Arrange
        mockUserRole(false);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getAllPayments(authentication))
                .isInstanceOf(CustomAccessDeniedException.class)
                .hasMessageContaining("Only admin can view all payments.");

        verify(paymentRepository, never()).findAll();
    }

    // --- Тесты для updatePaymentStatus ---

    @Test
    @DisplayName("Администратор должен успешно обновить статус платежа")
    void updatePaymentStatus_ShouldSucceed_ForAdmin() {
        // Arrange
        PaymentStatusRequestDTO requestDTO = new PaymentStatusRequestDTO();
        requestDTO.setStatus("FAILED");

        mockUserRole(true);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        paymentService.updatePaymentStatus(1L, requestDTO, authentication);

        // Assert
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());

        Payment savedPayment = captor.getValue();
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(savedPayment.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Не-администратор не должен обновлять статус")
    void updatePaymentStatus_ShouldThrowAccessDenied_ForNonAdmin() {
        // Arrange
        PaymentStatusRequestDTO requestDTO = new PaymentStatusRequestDTO();
        requestDTO.setStatus("SUCCESS");
        mockUserRole(false);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.updatePaymentStatus(1L, requestDTO, authentication))
                .isInstanceOf(CustomAccessDeniedException.class)
                .hasMessageContaining("Only admin can update payment status.");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен выбросить InvalidPaymentDataException при null статусе")
    void updatePaymentStatus_ShouldThrowException_ForNullStatus() {
        // Arrange
        mockUserRole(true);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.updatePaymentStatus(1L, null, authentication))
                .isInstanceOf(InvalidPaymentDataException.class)
                .hasMessageContaining("Payment status cannot be null");
    }

    // --- Вспомогательные методы ---
    private void mockUserRole(boolean isAdmin) {
        Collection<GrantedAuthority> authorities = isAdmin
                ? List.of(new SimpleGrantedAuthority(adminRole))
                : Collections.emptyList();
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
    }
}
