package com.danialrekhman.orderservicenorcurne.repository;

import com.danialrekhman.orderservicenorcurne.model.OrderItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Найти все товары в конкретном заказе
    List<OrderItem> findAllByOrderId(Long orderId);

    @Query("select o.userEmail from OrderItem oi join oi.order o where oi.id = :orderItemId")
    String findUserEmailByOrderItemId(Long orderItemId);

    // Найти товар в заказе по ID продукта
    Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

    // Посчитать количество товаров в заказе
    long countByOrderId(Long orderId);

    // Удалить все позиции по ID заказа (например, при отмене)
    void deleteByOrderId(Long orderId);
}
