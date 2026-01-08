package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUser() {
        UserDto userDto = new UserDto();
        userDto.setName("Иван");
        userDto.setEmail("ivan@example.com");

        User user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto created = userService.createUser(userDto);

        assertNotNull(created.getId());
        assertEquals("Иван", created.getName());
        assertEquals("ivan@example.com", created.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        UserDto userDto = new UserDto();
        userDto.setName("Иван");
        userDto.setEmail("test@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(userDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateUser() {
        Long userId = 1L;
        UserDto updateDto = new UserDto();
        updateDto.setName("Пётр");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Иван");
        existingUser.setEmail("ivan@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDto updated = userService.updateUser(userId, updateDto);

        assertEquals("Пётр", updated.getName());
        assertEquals("ivan@example.com", updated.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Иван");
        user1.setEmail("ivan@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Пётр");
        user2.setEmail("petr@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void shouldDeleteUser() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }
}