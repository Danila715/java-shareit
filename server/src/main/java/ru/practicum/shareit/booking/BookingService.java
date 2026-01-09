package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {

    BookingResponseDto addBooking(Long userId, BookingDto bookingDto);

    BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getBookingById(Long userId, Long bookingId);

    List<BookingResponseDto> getBookingsByUser(Long userId, BookingState state);

    List<BookingResponseDto> getBookingsByOwner(Long userId, BookingState state);
}