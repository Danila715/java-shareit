package ru.practicum.shareit.booking;

public enum BookingStatus {
    WAITING,    // Новое бронирование, ожидает одобрения
    APPROVED,   // Бронирование подтверждено владельцем
    REJECTED,   // Бронирование отклонено владельцем
    CANCELLED   // Бронирование отменено создателем
}
