package com.danialrekhman.productservicenocturne.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id;

    @Column(nullable = false)
    String name;

    // Parent category (can be null)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    Category parent;

    // Subcategories
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Category> subcategories = new ArrayList<>();

    // Products in category
    @OneToMany(mappedBy = "category")
    List<Product> products = new ArrayList<>();
}