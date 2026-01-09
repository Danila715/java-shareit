package ru.practicum.shareit.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
}
