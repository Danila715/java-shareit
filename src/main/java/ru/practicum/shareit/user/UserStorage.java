package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 1L;

    public User save(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void deleteById(Long id) {
        users.remove(id);
    }

    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    public boolean existsByEmailAndIdNot(String email, Long excludeUserId) {
        return users.values().stream()
                .filter(user -> !user.getId().equals(excludeUserId))
                .anyMatch(user -> user.getEmail().equals(email));
    }
}