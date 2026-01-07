package ru.practicum.shareit.booking;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = {"item", "booker"})
@EqualsAndHashCode(of = {"id"})
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    // Для обратной совместимости с существующим кодом
    @Transient
    public Long getItemId() {
        return item != null ? item.getId() : null;
    }

    @Transient
    public Long getBookerId() {
        return booker != null ? booker.getId() : null;
    }

    public void setItemId(Long itemId) {
        Item tempItem = new Item();
        tempItem.setId(itemId);
        this.item = tempItem;
    }

    public void setBookerId(Long bookerId) {
        User tempUser = new User();
        tempUser.setId(bookerId);
        this.booker = tempUser;
    }
}