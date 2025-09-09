package com.danialrekhman.orderservicenorcurne.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_table")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String userEmail;

    @Column(nullable = false)
    LocalDateTime orderDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderItem> items = new ArrayList<>();

    @Transient
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(item -> item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
