package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
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

        // Проверяем пользователя
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        // Проверяем вещь
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + bookingDto.getItemId() + " не найдена"));

        // Проверяем, что пользователь не владелец вещи - 404
        if (item.getOwner().equals(userId)) {
            throw new NotFoundException("Нельзя забронировать собственную вещь");
        }

        // Проверяем доступность вещи - 400 Bad Request (бизнес-логика)
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        // Проверяем корректность дат - 400 Bad Request (бизнес-логика)
        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new BadRequestException("Дата окончания должна быть после даты начала");
        }

        // Создаём бронирование
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

        // Получаем бронирование
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        // Получаем вещь
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + booking.getItemId() + " не найдена"));

        // Проверяем, что пользователь - владелец вещи - 403 Forbidden (недостаточно прав)
        if (!item.getOwner().equals(userId)) {
            throw new ForbiddenException("Подтвердить бронирование может только владелец вещи");
        }

        // Проверяем, что бронирование в статусе WAITING - 400 (бизнес-логика)
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Бронирование уже обработано");
        }

        // Обновляем статус
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        // Получаем букера
        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        log.info("Бронирование с id={} {}", bookingId, approved ? "подтверждено" : "отклонено");
        return BookingMapper.toBookingResponseDto(updatedBooking, booker, item);
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        log.info("Получение бронирования с id={} пользователем с id={}", bookingId, userId);

        // Получаем бронирование
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        // Получаем вещь
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!booking.getBookerId().equals(userId) && !item.getOwner().equals(userId)) {
            throw new ForbiddenException("Просмотреть бронирование может только автор или владелец вещи");
        }

        // Получаем букера
        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return BookingMapper.toBookingResponseDto(booking, booker, item);
    }

    @Override
    public List<BookingResponseDto> getBookingsByUser(Long userId, BookingState state) {
        log.info("Получение бронирований пользователя с id={}, state={}", userId, state);

        // Проверяем существование пользователя
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

        // Проверяем существование пользователя
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