package ru.practicum.shareit.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    @NotNull(message = "ID вещи обязателен")
    private Long itemId;

    @FutureOrPresent(message = "Дата начала не может быть в прошлом")
    @NotNull(message = "Дата начала обязательна")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания обязательна")
    private LocalDateTime end;
}