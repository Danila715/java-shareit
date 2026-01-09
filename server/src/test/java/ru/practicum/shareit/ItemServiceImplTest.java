package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void shouldAddItem() {
        Long userId = 1L;
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(userId);

        when(userService.userExists(userId)).thenReturn(true);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto created = itemService.addItem(userId, itemDto);

        assertNotNull(created);
        assertEquals("Дрель", created.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void shouldThrowExceptionWhenAddingItemForNonExistentUser() {
        Long userId = 999L;
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        when(userService.userExists(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.addItem(userId, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldUpdateItem() {
        Long userId = 1L;
        Long itemId = 1L;

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Дрель");
        existingItem.setDescription("Мощная дрель");
        existingItem.setAvailable(true);
        existingItem.setOwner(userId);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Дрель обновлённая");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        ItemDto updated = itemService.updateItem(userId, itemId, updateDto);

        assertEquals("Дрель обновлённая", updated.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerUpdatesItem() {
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long itemId = 1L;

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(ownerId);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новое название");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(otherUserId, itemId, updateDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldGetItemById() {
        Long itemId = 1L;
        Long userId = 1L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");
        item.setOwner(userId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(Collections.emptyList());
        when(bookingRepository.findLastBookingForItem(anyLong(), any())).thenReturn(null);
        when(bookingRepository.findNextBookingForItem(anyLong(), any())).thenReturn(null);

        ItemDto found = itemService.getItemById(itemId, userId);

        assertEquals(itemId, found.getId());
        assertEquals("Дрель", found.getName());
    }

    @Test
    void shouldSearchItems() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель Салют");
        item.setAvailable(true);

        when(itemRepository.searchByText("дрель")).thenReturn(List.of(item));

        List<ItemDto> found = itemService.searchItems("дрель");

        assertEquals(1, found.size());
        assertEquals("Дрель Салют", found.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListForEmptySearch() {
        List<ItemDto> found = itemService.searchItems("");

        assertTrue(found.isEmpty());
        verify(itemRepository, never()).searchByText(anyString());
    }

    @Test
    void getItemsByOwner_emptyList_shouldReturnEmpty() {
        Long userId = 1L;

        when(itemRepository.findAllByOwner(userId)).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getItemsByOwner(userId);

        assertTrue(result.isEmpty());

        verify(commentRepository).findAllByItemIdIn(eq(Collections.emptyList()));
    }

    @Test
    void getItemsByOwner_multipleItems_shouldBatchLoadComments() {
        Long userId = 1L;

        Item item1 = new Item();
        item1.setId(1L);
        item1.setOwner(userId);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setOwner(userId);

        when(itemRepository.findAllByOwner(userId)).thenReturn(List.of(item1, item2));

        Comment comment1 = new Comment();
        comment1.setItemId(1L);
        comment1.setAuthorId(3L);

        Comment comment2 = new Comment();
        comment2.setItemId(2L);
        comment2.setAuthorId(4L);

        when(commentRepository.findAllByItemIdIn(List.of(1L, 2L))).thenReturn(List.of(comment1, comment2));

        User author1 = new User();
        author1.setId(3L);
        author1.setName("Author1");

        User author2 = new User();
        author2.setId(4L);
        author2.setName("Author2");

        when(userRepository.findAllById(List.of(3L, 4L))).thenReturn(List.of(author1, author2));

        List<ItemDto> result = itemService.getItemsByOwner(userId);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getComments().size());
        assertEquals(1, result.get(1).getComments().size());
    }

    @Test
    void getItemById_notOwner_noBookingsAdded() {
        Long itemId = 1L;
        Long strangerId = 99L;
        Long ownerId = 2L;

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(ownerId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(Collections.emptyList());

        ItemDto result = itemService.getItemById(itemId, strangerId);

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        verify(bookingRepository, never()).findLastBookingForItem(anyLong(), any());
        verify(bookingRepository, never()).findNextBookingForItem(anyLong(), any());
    }

    @Test
    void updateItem_partialUpdate_onlyDescription() {
        Long userId = 1L;
        Long itemId = 1L;

        Item existing = new Item();
        existing.setId(itemId);
        existing.setName("Старое имя");
        existing.setDescription("Старое описание");
        existing.setAvailable(true);
        existing.setOwner(userId);

        ItemDto updateDto = new ItemDto();
        updateDto.setDescription("Новое описание");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenReturn(existing);

        ItemDto result = itemService.updateItem(userId, itemId, updateDto);

        assertEquals("Старое имя", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void addComment_itemNotExists_shouldThrowNotFound() {
        Long userId = 1L;
        Long itemId = 99L;

        CommentDto dto = new CommentDto();
        dto.setText("Текст");

        when(itemRepository.existsById(itemId)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(userId, itemId, dto));

        verify(bookingRepository, never()).existsCompletedBookingByBookerAndItem(anyLong(), anyLong(), any());
    }
}