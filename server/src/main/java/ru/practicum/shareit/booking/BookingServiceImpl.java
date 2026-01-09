package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto addBooking(Long userId, BookingDto bookingDto) {
        log.info("Создание бронирования пользователем с id={} для вещи с id={}",
                userId, bookingDto.getItemId());

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + bookingDto.getItemId() + " не найдена"));

        if (item.getOwner().equals(userId)) {
            throw new ValidationException("Нельзя бронировать свою собственную вещь");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, userId);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование создано с id={}", savedBooking.getId());
        return BookingMapper.toBookingResponseDto(savedBooking, booker, item);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение/отклонение бронирования с id={} пользователем с id={}",
                bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + booking.getItemId() + " не найдена"));

        if (!item.getOwner().equals(userId)) {
            throw new AccessDeniedException("Подтвердить бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        log.info("Бронирование с id={} {}", bookingId, approved ? "подтверждено" : "отклонено");
        return BookingMapper.toBookingResponseDto(updatedBooking, booker, item);
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        log.info("Получение бронирования с id={} пользователем с id={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!booking.getBookerId().equals(userId) && !item.getOwner().equals(userId)) {
            throw new AccessDeniedException("Просмотреть бронирование может только автор или владелец вещи");
        }

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return BookingMapper.toBookingResponseDto(booking, booker, item);
    }

    @Override
    public List<BookingResponseDto> getBookingsByUser(Long userId, BookingState state) {
        log.info("Получение бронирований пользователя с id={}, state={}", userId, state);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        List<Booking> bookings = getBookingsByState(userId, state, false);

        return bookings.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long userId, BookingState state) {
        log.info("Получение бронирований владельца с id={}, state={}", userId, state);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        List<Booking> bookings = getBookingsByState(userId, state, true);

        return bookings.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private List<Booking> getBookingsByState(Long userId, BookingState state, boolean isOwner) {
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case ALL -> isOwner
                    ? bookingRepository.findAllByItemOwnerId(userId)
                    : bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> isOwner
                    ? bookingRepository.findCurrentByItemOwnerId(userId, now)
                    : bookingRepository.findCurrentByBookerId(userId, now);
            case PAST -> isOwner
                    ? bookingRepository.findPastByItemOwnerId(userId, now)
                    : bookingRepository.findPastByBookerId(userId, now);
            case FUTURE -> isOwner
                    ? bookingRepository.findFutureByItemOwnerId(userId, now)
                    : bookingRepository.findFutureByBookerId(userId, now);
            case WAITING -> isOwner
                    ? bookingRepository.findAllByItemOwnerIdAndStatus(userId, BookingStatus.WAITING)
                    : bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> isOwner
                    ? bookingRepository.findAllByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED)
                    : bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
    }

    private BookingResponseDto mapToResponseDto(Booking booking) {
        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        return BookingMapper.toBookingResponseDto(booking, booker, item);
    }
}