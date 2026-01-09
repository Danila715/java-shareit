package ru.practicum.shareit.user;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.item.Item;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"items", "bookings", "comments"})
@EqualsAndHashCode(of = {"id"})
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 512)
    private String email;

    @OneToMany(mappedBy = "ownerEntity")
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "booker")
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();
}