package com.danialrekhman.orderservicenorcurne.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long productId;

    @Column(nullable = false)
    int quantity;

    @Column(nullable = false)
    BigDecimal priceAtOrder;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;
}
