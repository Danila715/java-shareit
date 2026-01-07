package ru.practicum.shareit.item;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "available", nullable = false)
    private Boolean available;

    @Column(name = "owner_id", nullable = false)
    private Long owner;
}