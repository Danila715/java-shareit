package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {

    private final ItemRequestService requestService;
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Test
    void createRequest_shouldSaveAndReturnRequest() {
        // given
        User user = createUser("user@test.com", "User");
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        // when
        ItemRequestResponseDto result = requestService.createRequest(user.getId(), dto);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();

        ItemRequest saved = requestRepository.findById(result.getId()).orElseThrow();
        assertThat(saved.getDescription()).isEqualTo("Нужна дрель");
    }

    @Test
    void createRequest_withNonExistentUser_shouldThrowNotFoundException() {
        // given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        // when & then
        assertThatThrownBy(() -> requestService.createRequest(999L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    @Test
    void getAllRequests_shouldReturnOnlyOthersRequests() {
        // given
        User user1 = createUser("user1@test.com", "User1");
        User user2 = createUser("user2@test.com", "User2");

        createRequest(user1, "Запрос юзера 1");
        createRequest(user2, "Запрос юзера 2");

        // when
        List<ItemRequestResponseDto> result = requestService.getAllRequests(user1.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Запрос юзера 2");
    }

    @Test
    void getRequestById_withNonExistentRequest_shouldThrowNotFoundException() {
        // given
        User user = createUser("user@test.com", "User");

        // when & then
        assertThatThrownBy(() -> requestService.getRequestById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос с id=999 не найден");
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    private ItemRequest createRequest(User requestor, String description) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(description);
        ItemRequestResponseDto response = requestService.createRequest(requestor.getId(), dto);
        return requestRepository.findById(response.getId()).orElseThrow();
    }

    private Item createItem(User owner, String name, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner.getId());
        if (request != null) {
            item.setRequest(request);
        }
        return itemRepository.save(item);
    }
}