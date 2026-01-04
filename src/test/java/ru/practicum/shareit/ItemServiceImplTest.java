package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class ItemServiceImplTest {

    private ItemService itemService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        itemService = new ItemServiceImpl(userService);

        // По умолчанию пользователи существуют
        when(userService.userExists(anyLong())).thenReturn(true);
    }

    @Test
    void shouldAddItem() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        ItemDto created = itemService.addItem(1L, itemDto);

        assertNotNull(created.getId());
        assertEquals("Дрель", created.getName());
        assertEquals("Мощная дрель", created.getDescription());
        assertTrue(created.getAvailable());
    }

    @Test
    void shouldThrowExceptionWhenAddingItemForNonExistentUser() {
        when(userService.userExists(999L)).thenReturn(false);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        assertThrows(NoSuchElementException.class, () -> itemService.addItem(999L, itemDto));
    }

    @Test
    void shouldUpdateItem() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);
        ItemDto created = itemService.addItem(1L, itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Дрель обновлённая");
        updateDto.setAvailable(false);

        ItemDto updated = itemService.updateItem(1L, created.getId(), updateDto);

        assertEquals("Дрель обновлённая", updated.getName());
        assertEquals("Мощная дрель", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerUpdatesItem() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);
        ItemDto created = itemService.addItem(1L, itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новое название");

        assertThrows(SecurityException.class,
                () -> itemService.updateItem(2L, created.getId(), updateDto));
    }

    @Test
    void shouldGetItemById() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);
        ItemDto created = itemService.addItem(1L, itemDto);

        ItemDto found = itemService.getItemById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Дрель", found.getName());
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        assertThrows(NoSuchElementException.class, () -> itemService.getItemById(999L));
    }

    @Test
    void shouldGetItemsByOwner() {
        ItemDto item1 = new ItemDto();
        item1.setName("Дрель");
        item1.setDescription("Мощная дрель");
        item1.setAvailable(true);

        ItemDto item2 = new ItemDto();
        item2.setName("Молоток");
        item2.setDescription("Тяжёлый молоток");
        item2.setAvailable(true);

        itemService.addItem(1L, item1);
        itemService.addItem(1L, item2);
        itemService.addItem(2L, item1); // Другой владелец

        List<ItemDto> items = itemService.getItemsByOwner(1L);

        assertEquals(2, items.size());
    }

    @Test
    void shouldSearchItems() {
        ItemDto item1 = new ItemDto();
        item1.setName("Дрель Салют");
        item1.setDescription("Мощная дрель для бетона");
        item1.setAvailable(true);

        ItemDto item2 = new ItemDto();
        item2.setName("Молоток");
        item2.setDescription("Ударный инструмент");
        item2.setAvailable(true);

        ItemDto item3 = new ItemDto();
        item3.setName("Дрель Makita");
        item3.setDescription("Профессиональная");
        item3.setAvailable(false); // Недоступна

        itemService.addItem(1L, item1);
        itemService.addItem(1L, item2);
        itemService.addItem(2L, item3);

        List<ItemDto> found = itemService.searchItems("дрель");

        assertEquals(1, found.size());
        assertEquals("Дрель Салют", found.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListForEmptySearch() {
        List<ItemDto> found = itemService.searchItems("");

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForNullSearch() {
        List<ItemDto> found = itemService.searchItems(null);

        assertTrue(found.isEmpty());
    }
}