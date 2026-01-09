package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto, Long requestorId) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestorId(requestorId);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest request) {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());

        List<ItemDto> items = request.getItems() != null && !request.getItems().isEmpty()
                ? request.getItems().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        dto.setItems(items);
        return dto;
    }
}