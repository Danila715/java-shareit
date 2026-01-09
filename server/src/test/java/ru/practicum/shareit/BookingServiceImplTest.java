package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void addBooking_success() {
        Long userId = 1L;
        Long itemId = 2L;

        User booker = new User();
        booker.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(3L);
        item.setAvailable(true);

        BookingDto dto = new BookingDto();
        dto.setItemId(itemId);
        dto.setStart(now.plusHours(1));
        dto.setEnd(now.plusHours(3));

        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setStatus(BookingStatus.WAITING);
        savedBooking.setBooker(booker);
        savedBooking.setItem(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponseDto result = bookingService.addBooking(userId, dto);

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void addBooking_itemNotFound_shouldThrowNotFound() {
        Long userId = 1L;
        BookingDto dto = new BookingDto();
        dto.setItemId(99L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(userId, dto));
    }

    @Test
    void addBooking_ownItem_shouldThrowValidation() {
        Long userId = 1L;
        Item item = new Item();
        item.setId(1L);
        item.setOwner(userId);

        BookingDto dto = new BookingDto();
        dto.setItemId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(userId, dto));
    }

    @Test
    void addBooking_itemNotAvailable_shouldThrowValidation() {
        Long userId = 1L;
        Item item = new Item();
        item.setId(1L);
        item.setOwner(2L);
        item.setAvailable(false);

        BookingDto dto = new BookingDto();
        dto.setItemId(1L);
        dto.setStart(now.plusHours(1));
        dto.setEnd(now.plusHours(2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(userId, dto));
    }

    @Test
    void addBooking_wrongDates_shouldThrowValidation() {
        Long userId = 1L;
        Item item = new Item();
        item.setId(1L);
        item.setOwner(2L);
        item.setAvailable(true);

        BookingDto dto = new BookingDto();
        dto.setItemId(1L);
        dto.setStart(now.plusHours(2));
        dto.setEnd(now.plusHours(1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(userId, dto));
    }

    @Test
    void approveBooking_success_approved() {
        Long ownerId = 2L;
        Long bookingId = 1L;
        Long bookerId = 3L;

        Item item = new Item();
        item.setId(1L);
        item.setOwner(ownerId);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(bookerId);
        booking.setStatus(BookingStatus.WAITING);

        User booker = new User();
        booker.setId(bookerId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.approveBooking(ownerId, bookingId, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(userRepository).findById(bookerId);
    }

    @Test
    void approveBooking_alreadyProcessed_shouldThrowValidation() {
        Long ownerId = 2L;
        Long bookingId = 1L;

        Item item = new Item();
        item.setOwner(ownerId);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(3L);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(ownerId, bookingId, true));
    }

    @Test
    void approveBooking_notOwner_shouldThrowAccessDenied() {
        Long notOwnerId = 99L;
        Long bookingId = 1L;

        Item item = new Item();
        item.setOwner(2L);

        Booking booking = new Booking();
        booking.setItemId(1L);
        booking.setBookerId(3L);  // задаём

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.approveBooking(notOwnerId, bookingId, true));
    }

    @Test
    void getBookingById_success_booker() {
        Long userId = 1L;
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(userId);

        Item item = new Item();
        item.setId(1L);
        item.setOwner(2L);

        User booker = new User();
        booker.setId(userId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));

        BookingResponseDto result = bookingService.getBookingById(userId, bookingId);

        assertNotNull(result);
    }

    @Test
    void getBookingById_success_owner() {
        Long ownerId = 2L;
        Long bookingId = 1L;
        Long bookerId = 3L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(bookerId);

        Item item = new Item();
        item.setId(1L);
        item.setOwner(ownerId);

        User booker = new User();
        booker.setId(bookerId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));

        BookingResponseDto result = bookingService.getBookingById(ownerId, bookingId);

        assertNotNull(result);
    }

    @Test
    void getBookingById_notAuthorized_shouldThrowAccessDenied() {
        Long strangerId = 99L;
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setBookerId(1L);
        booking.setItemId(1L);

        Item item = new Item();
        item.setOwner(2L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.getBookingById(strangerId, bookingId));
    }

    @Test
    void getBookingsByUser_allStates_covered() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        bookingService.getBookingsByUser(userId, BookingState.ALL);
        bookingService.getBookingsByUser(userId, BookingState.CURRENT);
        bookingService.getBookingsByUser(userId, BookingState.PAST);
        bookingService.getBookingsByUser(userId, BookingState.FUTURE);
        bookingService.getBookingsByUser(userId, BookingState.WAITING);
        bookingService.getBookingsByUser(userId, BookingState.REJECTED);

        verify(bookingRepository, atLeastOnce()).findAllByBookerIdOrderByStartDesc(userId);
    }

    @Test
    void getBookingsByOwner_allStates_covered() {
        Long ownerId = 1L;

        when(userRepository.existsById(ownerId)).thenReturn(true);

        bookingService.getBookingsByOwner(ownerId, BookingState.ALL);
        bookingService.getBookingsByOwner(ownerId, BookingState.CURRENT);
        bookingService.getBookingsByOwner(ownerId, BookingState.PAST);
        bookingService.getBookingsByOwner(ownerId, BookingState.FUTURE);
        bookingService.getBookingsByOwner(ownerId, BookingState.WAITING);
        bookingService.getBookingsByOwner(ownerId, BookingState.REJECTED);

        verify(bookingRepository, atLeastOnce()).findAllByItemOwnerId(ownerId);
    }

    @Test
    void getBookingsByUser_userNotFound_shouldThrowNotFound() {
        Long userId = 999L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByUser(userId, BookingState.ALL));
    }

    @Test
    void getBookingsByOwner_userNotFound_shouldThrowNotFound() {
        Long userId = 999L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByOwner(userId, BookingState.ALL));
    }

    @Test
    void getBookingsByUser_emptyList_shouldReturnEmpty() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(userId)).thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getBookingsByUser(userId, BookingState.ALL);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBookingsByOwner_withBookings_shouldMapCorrectly() {
        Long ownerId = 1L;
        Long bookerId = 2L;

        when(userRepository.existsById(ownerId)).thenReturn(true);

        User booker = new User();
        booker.setId(bookerId);
        booker.setName("Booker");

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItemId(1L);
        booking.setBookerId(bookerId);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findAllByItemOwnerId(ownerId)).thenReturn(List.of(booking));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, BookingState.ALL);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getBooker());
        assertNotNull(result.get(0).getItem());
    }
}