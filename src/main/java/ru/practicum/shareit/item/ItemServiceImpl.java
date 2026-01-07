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
        log.info("Добавление вещи '{}' пользователем с id={}", itemDto.getName(), userId);

        if (!userService.userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Item item = ItemMapper.toItem(itemDto, userId);
        Item savedItem = itemRepository.save(item);

        log.info("Вещь добавлена с id={}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи с id={} пользователем с id={}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!item.getOwner().equals(userId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
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

        Item updatedItem = itemRepository.save(item);
        log.info("Вещь с id={} обновлена", itemId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Получение вещи с id={}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        ItemDto itemDto = ItemMapper.toItemDto(item);

        // Добавляем бронирования только для владельца
        if (item.getOwner().equals(userId)) {
            addBookingsToItem(itemDto);
        }

        // Добавляем комментарии
        addCommentsToItem(itemDto);

        return itemDto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        log.info("Получение списка вещей пользователя с id={}", userId);

        List<Item> items = itemRepository.findAllByOwner(userId);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        // Добавляем бронирования ко всем вещам
        itemDtos.forEach(this::addBookingsToItem);

        // Добавляем комментарии
        addCommentsToItems(itemDtos);

        return itemDtos;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);

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
        log.info("Добавление комментария к вещи с id={} пользователем с id={}", itemId, userId);

        // Проверяем существование вещи
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Вещь с id=" + itemId + " не найдена");
        }

        // Проверяем существование пользователя
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        // Проверяем, что пользователь брал вещь в аренду - должен быть 400 Bad Request
        boolean hasBooking = bookingRepository.existsCompletedBookingByBookerAndItem(
                userId, itemId, LocalDateTime.now());

        if (!hasBooking) {
            throw new IllegalArgumentException("Оставить комментарий может только пользователь, " +
                    "который брал вещь в аренду");
        }

        Comment comment = CommentMapper.toComment(commentDto, itemId, userId);
        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий добавлен с id={}", savedComment.getId());
        return CommentMapper.toCommentDto(savedComment, author.getName());
    }

    private void addBookingsToItem(ItemDto itemDto) {
        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = bookingRepository.findLastBookingForItem(itemDto.getId(), now);
        Booking nextBooking = bookingRepository.findNextBookingForItem(itemDto.getId(), now);

        itemDto.setLastBooking(BookingMapper.toBookingShortDto(lastBooking));
        itemDto.setNextBooking(BookingMapper.toBookingShortDto(nextBooking));
    }

    private void addCommentsToItem(ItemDto itemDto) {
        List<Comment> comments = commentRepository.findAllByItemId(itemDto.getId());

        List<Long> authorIds = comments.stream()
                .map(Comment::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> authorNames = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> CommentMapper.toCommentDto(comment, authorNames.get(comment.getAuthorId())))
                .collect(Collectors.toList());

        itemDto.setComments(commentDtos);
    }

    private void addCommentsToItems(List<ItemDto> itemDtos) {
        List<Long> itemIds = itemDtos.stream()
                .map(ItemDto::getId)
                .collect(Collectors.toList());

        List<Comment> comments = commentRepository.findAllByItemIdIn(itemIds);

        List<Long> authorIds = comments.stream()
                .map(Comment::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> authorNames = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        Map<Long, List<CommentDto>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(
                        Comment::getItemId,
                        Collectors.mapping(
                                comment -> CommentMapper.toCommentDto(comment, authorNames.get(comment.getAuthorId())),
                                Collectors.toList()
                        )
                ));

        itemDtos.forEach(itemDto ->
                itemDto.setComments(commentsByItem.getOrDefault(itemDto.getId(), Collections.emptyList()))
        );
    }
}