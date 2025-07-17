package com.danialrekhman.orderservicenorcurne.repository;

import com.danialrekhman.orderservicenorcurne.model.Order;
import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserEmail(String userEmail);

    @Query("select o.userEmail from Order o where o.id = :orderId")
    String findUserEmailById(Long orderId);
    
    List<Order> findAllByStatus(OrderStatus status);

    // Получить заказы, созданные после определенной даты
    List<Order> findAllByOrderDateAfter(LocalDateTime orderDateAfter);

    // Получить заказы, оформленные в определённый диапазон времени
    List<Order> findAllByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    // Проверить, существует ли хотя бы один заказ у пользователя
    boolean existsByUserEmail(String email);

    // Посчитать количество заказов пользователя
    long countByUserEmail(String email);
}
