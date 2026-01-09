package ru.practicum.shareit.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

public class UserClient extends BaseClient {

    public UserClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> createUser(UserDto dto) {
        return post("", 0L, dto);
    }

    public ResponseEntity<Object> getUser(long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> updateUser(long userId, UserDto dto) {
        return patch("/" + userId, userId, dto);
    }

    public ResponseEntity<Object> deleteUser(long userId) {
        return delete("/" + userId, userId);
    }
}