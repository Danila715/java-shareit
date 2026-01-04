package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
    }

    @Test
    void shouldCreateUser() {
        UserDto userDto = new UserDto();
        userDto.setName("Иван");
        userDto.setEmail("ivan@example.com");

        UserDto created = userService.createUser(userDto);

        assertNotNull(created.getId());
        assertEquals("Иван", created.getName());
        assertEquals("ivan@example.com", created.getEmail());
    }

    @Test
    void shouldUpdateUser() {
        UserDto userDto = new UserDto();
        userDto.setName("Иван");
        userDto.setEmail("ivan@example.com");
        UserDto created = userService.createUser(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("Пётр");

        UserDto updated = userService.updateUser(created.getId(), updateDto);

        assertEquals("Пётр", updated.getName());
        assertEquals("ivan@example.com", updated.getEmail());
    }

    @Test
    void shouldGetUserById() {
        UserDto userDto = new UserDto();
        userDto.setName("Иван");
        userDto.setEmail("ivan@example.com");
        UserDto created = userService.createUser(userDto);

        UserDto found = userService.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Иван", found.getName());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThrows(NoSuchElementException.class, () -> userService.getUserById(999L));
    }

    @Test
    void shouldGetAllUsers() {
        UserDto user1 = new UserDto();
        user1.setName("Иван");
        user1.setEmail("ivan@example.com");

        UserDto user2 = new UserDto();
        user2.setName("Пётр");
        user2.setEmail("petr@example.com");

        userService.createUser(user1);
        userService.createUser(user2);

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void shouldDeleteUser() {
        UserDto userDto = new UserDto();
        userDto.setName("Иван");
        userDto.setEmail("ivan@example.com");
        UserDto created = userService.createUser(userDto);

        userService.deleteUser(created.getId());

        assertThrows(NoSuchElementException.class, () -> userService.getUserById(created.getId()));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        UserDto user1 = new UserDto();
        user1.setName("Иван");
        user1.setEmail("test@example.com");

        UserDto user2 = new UserDto();
        user2.setName("Пётр");
        user2.setEmail("test@example.com");

        userService.createUser(user1);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user2));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {
        UserDto user1 = new UserDto();
        user1.setName("Иван");
        user1.setEmail("ivan@example.com");

        UserDto user2 = new UserDto();
        user2.setName("Пётр");
        user2.setEmail("petr@example.com");

        UserDto created1 = userService.createUser(user1);
        UserDto created2 = userService.createUser(user2);

        UserDto updateDto = new UserDto();
        updateDto.setEmail("petr@example.com");

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(created1.getId(), updateDto));
    }
}