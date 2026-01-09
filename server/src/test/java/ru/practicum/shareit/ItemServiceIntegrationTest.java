package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegrationTest {

    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Test
    void addItem_shouldSaveAndReturn() {
        // given
        User owner = createUser("owner@test.com", "Owner");
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Мощная дрель");
        dto.setAvailable(true);

        // when
        ItemDto result = itemService.addItem(owner.getId(), dto);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Дрель");

        Item saved = itemRepository.findById(result.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Дрель");
    }

    @Test
    void updateItem_shouldUpdateFields() {
        // given
        User owner = createUser("owner@test.com", "Owner");
        Item item = createItem(owner, "Дрель", "Старое описание");

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Перфоратор");
        updateDto.setDescription("Новое описание");

        // when
        ItemDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        // then
        assertThat(result.getName()).isEqualTo("Перфоратор");
        assertThat(result.getDescription()).isEqualTo("Новое описание");
    }

    @Test
    void updateItem_byNonOwner_shouldThrowForbiddenException() {
        // given
        User owner = createUser("owner@test.com", "Owner");
        User other = createUser("other@test.com", "Other");
        Item item = createItem(owner, "Дрель", "Описание");

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новое имя");

        // when & then
        assertThatThrownBy(() -> itemService.updateItem(other.getId(), item.getId(), updateDto))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getItemsByUser_shouldReturnWithBookings() {
        // given
        User owner = createUser("owner@test.com", "Owner");
        User booker = createUser("booker@test.com", "Booker");
        Item item = createItem(owner, "Дрель", "Описание");

        Booking lastBooking = createBooking(booker, item,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                BookingStatus.APPROVED);

        Booking nextBooking = createBooking(booker, item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED);

        // when
        List<ItemDto> result = itemService.getItemsByOwner(owner.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastBooking()).isNotNull();
        assertThat(result.get(0).getNextBooking()).isNotNull();
    }

    @Test
    void addComment_shouldSaveAndReturn() {
        // given
        User owner = createUser("owner@test.com", "Owner");
        User booker = createUser("booker@test.com", "Booker");
        Item item = createItem(owner, "Дрель", "Описание");

        createBooking(booker, item,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                BookingStatus.APPROVED);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель!");

        // when
        CommentDto result = itemService.addComment(booker.getId(), item.getId(), commentDto);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Отличная дрель!");
        assertThat(result.getAuthorName()).isEqualTo("Booker");
    }

    @Test
    void addComment_withoutCompletedBooking_shouldThrowBadRequestException() {
        // given
        User owner = createUser("owner@test.com", "Owner");
        User user = createUser("user@test.com", "User");
        Item item = createItem(owner, "Дрель", "Описание");

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Комментарий");

        // when & then
        assertThatThrownBy(() -> itemService.addComment(user.getId(), item.getId(), commentDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("брал вещь в аренду");
    }

    @Test
    void searchItems_shouldReturnOnlyAvailable() {
        // given
        User owner = createUser("owner@test.com", "Owner");
        createItem(owner, "Дрель", "Мощная дрель").setAvailable(true);
        Item unavailable = createItem(owner, "Дрель 2", "Сломанная дрель");
        unavailable.setAvailable(false);
        itemRepository.save(unavailable);

        // when
        List<ItemDto> result = itemService.searchItems("дрель");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель");
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    private Item createItem(User owner, String name, String description) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(owner.getId());
        return itemRepository.save(item);
    }

    private Booking createBooking(User booker, Item item, LocalDateTime start,
                                  LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setBookerId(booker.getId());
        booking.setItemId(item.getId());
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}