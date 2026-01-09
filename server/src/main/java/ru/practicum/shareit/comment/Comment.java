package ru.practicum.shareit.comment;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = {"item", "author"})
@EqualsAndHashCode(of = {"id"})
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    // Для обратной совместимости с существующим кодом
    @Transient
    public Long getItemId() {
        return item != null ? item.getId() : null;
    }

    @Transient
    public Long getAuthorId() {
        return author != null ? author.getId() : null;
    }

    public void setItemId(Long itemId) {
        Item tempItem = new Item();
        tempItem.setId(itemId);
        this.item = tempItem;
    }

    public void setAuthorId(Long authorId) {
        User tempUser = new User();
        tempUser.setId(authorId);
        this.author = tempUser;
    }
}