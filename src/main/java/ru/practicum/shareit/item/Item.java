package ru.practicum.shareit.item;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.user.User;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"ownerEntity", "comments"})
@EqualsAndHashCode(of = {"id"})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User ownerEntity;

    @OneToMany(mappedBy = "item")
    private List<Comment> comments = new ArrayList<>();

    // Для обратной совместимости с существующим кодом
    @Transient
    public Long getOwner() {
        return ownerEntity != null ? ownerEntity.getId() : null;
    }

    public void setOwner(Long ownerId) {
        // Создаём временный объект User с ID для Hibernate
        User tempUser = new User();
        tempUser.setId(ownerId);
        this.ownerEntity = tempUser;
    }
}