package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        if (!userService.userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        Item item = ItemMapper.toItem(itemDto, userId);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().equals(userId)) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto dto = ItemMapper.toItemDto(item);

        if (item.getOwner().equals(userId)) {
            addBookings(dto);
        }

        addComments(dto);
        return dto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        List<ItemDto> items = itemRepository.findAllByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        items.forEach(this::addBookings);
        addComments(items);
        return items;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {

        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Вещь не найдена");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        boolean hasCompletedBooking =
                bookingRepository.existsCompletedBookingByBookerAndItem(
                        userId, itemId, LocalDateTime.now()
                );

        if (!hasCompletedBooking) {
            throw new ValidationException(
                    "Оставить комментарий может только пользователь, который брал вещь в аренду"
            );
        }

        Comment comment = CommentMapper.toComment(commentDto, itemId, userId);
        Comment saved = commentRepository.save(comment);

        return CommentMapper.toCommentDto(saved, author.getName());
    }

    private void addBookings(ItemDto itemDto) {
        LocalDateTime now = LocalDateTime.now();
        Booking last = bookingRepository.findLastBookingForItem(itemDto.getId(), now);
        Booking next = bookingRepository.findNextBookingForItem(itemDto.getId(), now);
        itemDto.setLastBooking(BookingMapper.toBookingShortDto(last));
        itemDto.setNextBooking(BookingMapper.toBookingShortDto(next));
    }

    private void addComments(ItemDto itemDto) {
        List<Comment> comments = commentRepository.findAllByItemId(itemDto.getId());
        mapComments(itemDto, comments);
    }

    private void addComments(List<ItemDto> items) {
        List<Long> ids = items.stream().map(ItemDto::getId).collect(Collectors.toList());
        List<Comment> comments = commentRepository.findAllByItemIdIn(ids);

        Map<Long, List<Comment>> grouped =
                comments.stream().collect(Collectors.groupingBy(Comment::getItemId));

        items.forEach(item ->
                mapComments(item, grouped.getOrDefault(item.getId(), Collections.emptyList()))
        );
    }

    private void mapComments(ItemDto itemDto, List<Comment> comments) {
        Map<Long, String> authors = userRepository.findAllById(
                        comments.stream().map(Comment::getAuthorId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        List<CommentDto> result = comments.stream()
                .map(c -> CommentMapper.toCommentDto(c, authors.get(c.getAuthorId())))
                .collect(Collectors.toList());

        itemDto.setComments(result);
    }
}
