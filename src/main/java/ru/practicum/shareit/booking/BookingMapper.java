package ru.practicum.shareit.booking;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {

    public static BookingResponseDto toBookingResponseDto(Booking booking, User booker, Item item) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());
        dto.setBooker(UserMapper.toUserDto(booker));
        dto.setItem(ItemMapper.toItemDto(item));
        return dto;
    }

    public static Booking toBooking(BookingDto bookingDto, Long bookerId) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItemId(bookingDto.getItemId());
        booking.setBookerId(bookerId);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBookerId());
        return dto;
    }
}