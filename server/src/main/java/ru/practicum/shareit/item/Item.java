package ru.practicum.shareit.item;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"ownerEntity", "comments", "request"})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ItemRequest request;

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

    @Transient
    public Long getRequestId() {
        return request != null ? request.getId() : null;
    }

    public void setRequestId(Long requestId) {
        if (requestId != null) {
            ItemRequest tempRequest = new ItemRequest();
            tempRequest.setId(requestId);
            this.request = tempRequest;
        } else {
            this.request = null;
        }
    }
}