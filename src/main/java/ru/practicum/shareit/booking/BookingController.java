package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto addBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long bookingId,
                                             @RequestParam Boolean approved) {
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingsByUser(@RequestHeader(USER_ID_HEADER) Long userId,
                                                      @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getBookingsByUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsByOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                       @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getBookingsByOwner(userId, state);
    }
}