package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto.getEmail());

        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Email уже используется");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userStorage.save(user);

        log.info("Пользователь создан с id={}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя с id={}", userId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (userStorage.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                throw new ConflictException("Email уже используется");
            }
            user.setEmail(userDto.getEmail());
        }

        User updatedUser = userStorage.update(user);
        log.info("Пользователь с id={} обновлён", userId);

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя с id={}", userId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение списка всех пользователей");

        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id={}", userId);

        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        userStorage.deleteById(userId);
        log.info("Пользователь с id={} удалён", userId);
    }

    @Override
    public boolean userExists(Long userId) {
        return userStorage.existsById(userId);
    }
}