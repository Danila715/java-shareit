package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestDto dto) {
        log.info("Создание запроса вещи пользователем с id={}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, userId);
        ItemRequest savedRequest = requestRepository.save(request);

        log.info("Запрос создан с id={}", savedRequest.getId());
        return ItemRequestMapper.toItemRequestResponseDto(savedRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        log.info("Получение своих запросов пользователем с id={}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        log.info("Получение чужих запросов пользователем с id={}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        return requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса с id={} пользователем с id={}", requestId, userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        return ItemRequestMapper.toItemRequestResponseDto(request);
    }
}