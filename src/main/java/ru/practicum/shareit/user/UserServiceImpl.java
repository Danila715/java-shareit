package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 1L;

    @Override
    public UserDto createUser(UserDto userDto) {
        validateEmail(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        user.setId(currentId++);
        users.put(user.getId(), user);

        log.info("Создан пользователь с id={}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с id=" + userId + " не найден");
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            validateEmail(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }

        log.info("Обновлен пользователь с id={}", userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с id=" + userId + " не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NoSuchElementException("Пользователь с id=" + userId + " не найден");
        }
        users.remove(userId);
        log.info("Удален пользователь с id={}", userId);
    }

    @Override
    public boolean userExists(Long userId) {
        return users.containsKey(userId);
    }

    private void validateEmail(String email, Long excludeUserId) {
        boolean emailExists = users.values().stream()
                .filter(user -> excludeUserId == null || !user.getId().equals(excludeUserId))
                .anyMatch(user -> user.getEmail().equals(email));

        if (emailExists) {
            throw new IllegalArgumentException("Email уже используется");
        }
    }
}